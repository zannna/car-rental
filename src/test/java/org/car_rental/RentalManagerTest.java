package org.car_rental;

import org.car_rental.dto.RentalConfirmationDto;
import org.car_rental.exception.CarReturnedAfterTimeException;
import org.car_rental.exception.NoAvailableCarsException;
import org.car_rental.exception.ReservationNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalManagerTest {

    private VehicleProvider vehicleProvider;
    private Map<UUID, Vehicle> rentalMap;
    private RentalManager manager;

    @BeforeEach
    void setUp() {
        vehicleProvider = mock(VehicleProvider.class);
        rentalMap = new HashMap<>();
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-01-01T10:00:00Z"),
                ZoneOffset.UTC
        );
        manager = new RentalManager(rentalMap, vehicleProvider, fixedClock);
    }

    @Test
    void shouldReserve_whenVehicleAvailable() {
        Vehicle vehicle = mock(Vehicle.class);

        Instant start = Instant.parse("2025-02-01T10:00:00Z");
        Instant end = start.plus(2, ChronoUnit.DAYS);

        when(vehicleProvider.findVehiclesFortType(VehicleType.SEDAN))
                .thenReturn(List.of(vehicle));

        UUID rentalId = UUID.randomUUID();

        Rental rental = new Rental(
                rentalId,
                start,
                end,
                "user",
                RentalStatus.RESERVED
        );

        when(vehicle.reserve(start, end, "user"))
                .thenReturn(Optional.of(rental));

        when(vehicle.getVehicleId()).thenReturn("V1");

        RentalConfirmationDto result =
                manager.reserve("user", VehicleType.SEDAN, start, 2);

        assertNotNull(result);
        assertEquals(rentalId, result.rentalId());
        assertEquals("V1", result.vehicleId());
        assertEquals(start, result.startDate());
        assertEquals(end, result.endDate());

        verify(vehicle).reserve(start, end, "user");
        assertTrue(rentalMap.containsKey(rentalId));
    }

    @Test
    void shouldThrow_whenValidationFails_userIdNull() {
        Instant start = Instant.parse("2025-02-01T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () ->
                manager.reserve(null, VehicleType.SEDAN, start, 2)
        );
    }

    @Test
    void shouldThrow_whenValidationFails_pastDate() {
        Instant past = Instant.parse("2024-01-01T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () ->
                manager.reserve("user", VehicleType.SEDAN, past, 2)
        );
    }

    @Test
    void shouldReserve_whenFirstOccupiedSecondAvailable() {
        Vehicle occupied = mock(Vehicle.class);
        Vehicle available = mock(Vehicle.class);

        when(vehicleProvider.findVehiclesFortType(VehicleType.SEDAN))
                .thenReturn(List.of(occupied, available));

        Instant start = Instant.parse("2025-02-01T10:00:00Z");
        Instant end = start.plus(2, ChronoUnit.DAYS);

        UUID rentalId = UUID.randomUUID();

        Rental rental = new Rental(
                rentalId,
                start,
                end,
                "user",
                RentalStatus.RESERVED
        );

        when(occupied.reserve(start, end, "user"))
                .thenReturn(Optional.empty());

        when(available.reserve(start, end, "user"))
                .thenReturn(Optional.of(rental));

        when(available.getVehicleId()).thenReturn("V2");

        RentalConfirmationDto result =
                manager.reserve("user", VehicleType.SEDAN, start, 2);

        assertNotNull(result);
        assertEquals("V2", result.vehicleId());

        verify(occupied).reserve(start, end, "user");
        verify(available).reserve(start, end, "user");
    }

    @Test
    void shouldStopAfterFirstSuccessfulReservation() {
        Vehicle v1 = mock(Vehicle.class);
        Vehicle v2 = mock(Vehicle.class);

        when(vehicleProvider.findVehiclesFortType(VehicleType.SEDAN))
                .thenReturn(List.of(v1, v2));

        Instant start = Instant.parse("2025-02-01T10:00:00Z");
        Instant end = start.plus(2, ChronoUnit.DAYS);

        Rental rental = new Rental(
                UUID.randomUUID(),
                start,
                end,
                "user",
                RentalStatus.RESERVED
        );

        when(v1.reserve(start, end, "user"))
                .thenReturn(Optional.of(rental));

        manager.reserve("user", VehicleType.SEDAN, start, 2);

        verify(v1).reserve(start, end, "user");
        verifyNoInteractions(v2);
    }

    @Test
    void shouldThrow_whenNoVehicleAvailable() {
        Vehicle v1 = mock(Vehicle.class);
        Vehicle v2 = mock(Vehicle.class);

        when(vehicleProvider.findVehiclesFortType(VehicleType.SEDAN))
                .thenReturn(List.of(v1, v2));

        Instant start = Instant.parse("2025-02-01T10:00:00Z");

        when(v1.reserve(any(), any(), any()))
                .thenReturn(Optional.empty());

        when(v2.reserve(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(NoAvailableCarsException.class, () ->
                manager.reserve("user", VehicleType.SEDAN, start, 2)
        );
    }

    @Test
    void shouldRentCar_whenVehicleFound() {
        Vehicle vehicle = mock(Vehicle.class);
        UUID rentalId = UUID.randomUUID();

        rentalMap.put(rentalId, vehicle);

        manager.rentCar(rentalId);

        verify(vehicle).rent(rentalId);
    }

    @Test
    void shouldCancelReservation_whenVehicleFound() {
        Vehicle vehicle = mock(Vehicle.class);
        UUID rentalId = UUID.randomUUID();

        rentalMap.put(rentalId, vehicle);

        manager.cancelReservation(rentalId);

        verify(vehicle).cancel(rentalId);
        assertTrue(rentalMap.isEmpty());
    }

    @Test
    void shouldReturnCar_whenVehicleFound() {
        Vehicle vehicle = mock(Vehicle.class);
        UUID rentalId = UUID.randomUUID();

        rentalMap.put(rentalId, vehicle);

        manager.returnCar(rentalId);

        verify(vehicle).returnCar(rentalId);
        assertTrue(rentalMap.isEmpty());
    }

    @Test
    void shouldThrow_whenCarReturnedAfterTime() {
        Vehicle vehicle = mock(Vehicle.class);
        UUID rentalId = UUID.randomUUID();

        rentalMap.put(rentalId, vehicle);

        doThrow(new CarReturnedAfterTimeException("late"))
                .when(vehicle).returnCar(rentalId);

        assertThrows(CarReturnedAfterTimeException.class, () ->
                manager.returnCar(rentalId)
        );

        assertTrue(rentalMap.isEmpty());
    }

    @Test
    void shouldThrow_whenVehicleNotFound() {
        UUID rentalId = UUID.randomUUID();

        assertThrows(ReservationNotExistsException.class, () ->
                manager.rentCar(rentalId)
        );
    }

}