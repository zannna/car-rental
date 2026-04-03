package org.car_rental;

import org.car_rental.exception.CarReturnedAfterTimeException;
import org.car_rental.exception.RentalStatusCanNotBeChangedException;
import org.car_rental.exception.ReservationNotExistsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private final String USER_ID = "user1";
    private final Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC);
    
    @Test
    void shouldReserve_whenScheduleIsEmpty() {
        // given
        Vehicle vehicle = emptyVehicle();
        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T12:00:00Z");

        // when
        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        // then
        assertTrue(result.isPresent());
        Rental rental = result.get();

        assertEquals(start, rental.getStartDate());
        assertEquals(end, rental.getEndDate());
        assertEquals(USER_ID, rental.getUserId());
        assertEquals(RentalStatus.RESERVED, rental.getStatus());
        assertNotNull(rental.getRentalId());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldReserve_whenNewEndEqualsExistingStart() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T12:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T12:00:00Z");

        // when
        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        // then
        assertTrue(result.isPresent());
        Rental rental = result.get();

        assertEquals(start, rental.getStartDate());
        assertEquals(end, rental.getEndDate());
        assertEquals(2, vehicle.getRentalSchedule().size());
        assertEquals(2, vehicle.getRentals().size());
    }

    @Test
    void shouldReserve_whenNewStartEqualsExistingEnd() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T12:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        Instant start = t("2025-01-01T14:00:00Z");
        Instant end = t("2025-01-01T16:00:00Z");

        // when
        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        // then
        assertTrue(result.isPresent());
        Rental rental = result.get();

        assertEquals(start, rental.getStartDate());
        assertEquals(end, rental.getEndDate());
        assertEquals(2, vehicle.getRentalSchedule().size());
        assertEquals(2, vehicle.getRentals().size());
    }

    @Test
    void shouldReserve_whenCompletelyBeforeExisting() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T12:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        Instant start = t("2024-01-01T08:00:00Z");
        Instant end = t("2024-01-01T10:00:00Z");

        // when
        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals(start, result.get().getStartDate());
        assertEquals(end, result.get().getEndDate());
        assertEquals(2, vehicle.getRentalSchedule().size());
        assertEquals(2, vehicle.getRentals().size());
    }

    @Test
    void shouldReserve_whenCompletelyAfterExisting() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T08:00:00Z"),
                t("2025-01-01T10:00:00Z")
        );

        Instant start = t("2025-02-01T12:00:00Z");
        Instant end = t("2025-02-01T14:00:00Z");

        // when
        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals(start, result.get().getStartDate());
        assertEquals(end, result.get().getEndDate());
        assertEquals(2, vehicle.getRentalSchedule().size());
        assertEquals(2, vehicle.getRentals().size());
    }



    @Test
    void shouldNotReserve_whenStartEqualsExistingStart() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        // when
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID
        );

        // then
        assertTrue(result.isEmpty());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenOverlapsFromLeft() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        // when
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T08:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID
        );

        // then
        assertTrue(result.isEmpty());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenCoversExisting() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        // when
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T08:00:00Z"),
                t("2025-01-01T16:00:00Z"),
                USER_ID
        );

        // then
        assertTrue(result.isEmpty());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenInsideExisting() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        // when
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T11:00:00Z"),
                t("2025-01-01T13:00:00Z"),
                USER_ID
        );

        // then
        assertTrue(result.isEmpty());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenOverlapsFromRight() {
        // given
        Vehicle vehicle = vehicleWithExistingRental(
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T14:00:00Z")
        );

        // when
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T11:00:00Z"),
                t("2025-01-01T16:00:00Z"),
                USER_ID
        );

        // then
        assertTrue(result.isEmpty());
        assertEquals(1, vehicle.getRentalSchedule().size());
        assertEquals(1, vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenOverlapsPreviousRental() {
        // GIVEN
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"));
        schedule.put(t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"));

        Map<UUID, Rental> rentals = new HashMap<>();
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();

        rentals.put(r1, new Rental(r1, t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"), USER_ID, RentalStatus.RESERVED));
        rentals.put(r2, new Rental(r2, t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"), USER_ID, RentalStatus.RESERVED));

        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());

        TreeMap<Instant, Instant> beforeSchedule = copySchedule(schedule);
        Map<UUID, Rental> beforeRentals = copyRentals(rentals);

        // WHEN
        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T11:00:00Z"),
                t("2025-01-01T13:00:00Z"),
                USER_ID
        );

        // THEN
        assertTrue(result.isEmpty());
        assertEquals(beforeSchedule, vehicle.getRentalSchedule());
        assertEquals(beforeRentals.size(), vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenOverlapsNextRental() {
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"));
        schedule.put(t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"));

        Map<UUID, Rental> rentals = new HashMap<>();
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();

        rentals.put(r1, new Rental(r1, t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"), USER_ID, RentalStatus.RESERVED));
        rentals.put(r2, new Rental(r2, t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"), USER_ID, RentalStatus.RESERVED));

        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());

        TreeMap<Instant, Instant> beforeSchedule = copySchedule(schedule);
        Map<UUID, Rental> beforeRentals = copyRentals(rentals);

        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T13:00:00Z"),
                t("2025-01-01T15:00:00Z"),
                USER_ID
        );

        assertTrue(result.isEmpty());
        assertEquals(beforeSchedule, vehicle.getRentalSchedule());
        assertEquals(beforeRentals.size(), vehicle.getRentals().size());
    }

    @Test
    void shouldNotReserve_whenOverlapsBothSides() {
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"));
        schedule.put(t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"));

        Map<UUID, Rental> rentals = new HashMap<>();
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();

        rentals.put(r1, new Rental(r1, t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"), USER_ID, RentalStatus.RESERVED));
        rentals.put(r2, new Rental(r2, t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"), USER_ID, RentalStatus.RESERVED));

        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());

        TreeMap<Instant, Instant> beforeSchedule = copySchedule(schedule);
        Map<UUID, Rental> beforeRentals = copyRentals(rentals);

        Optional<Rental> result = vehicle.reserve(
                t("2025-01-01T11:00:00Z"),
                t("2025-01-01T15:00:00Z"),
                USER_ID
        );

        assertTrue(result.isEmpty());
        assertEquals(beforeSchedule, vehicle.getRentalSchedule());
        assertEquals(beforeRentals.size(), vehicle.getRentals().size());
    }

    @Test
    void shouldReserve_whenExactlyFitsBetweenRentals() {
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"));
        schedule.put(t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"));

        Map<UUID, Rental> rentals = new HashMap<>();
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();

        rentals.put(r1, new Rental(r1, t("2025-01-01T10:00:00Z"), t("2025-01-01T12:00:00Z"), USER_ID, RentalStatus.RESERVED));
        rentals.put(r2, new Rental(r2, t("2025-01-01T14:00:00Z"), t("2025-01-01T16:00:00Z"), USER_ID, RentalStatus.RESERVED));

        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());

        int beforeSize = schedule.size();

        Instant start = t("2025-01-01T12:00:00Z");
        Instant end = t("2025-01-01T14:00:00Z");

        Optional<Rental> result = vehicle.reserve(start, end, USER_ID);

        assertTrue(result.isPresent());

        assertEquals(beforeSize + 1, vehicle.getRentalSchedule().size());
        assertEquals(beforeSize + 1, vehicle.getRentals().size());

        assertEquals(end, vehicle.getRentalSchedule().get(start));
    }
    @Test
    void shouldThrowException_whenRentAndReservationNotExists() {
        // GIVEN
        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN);

        // WHEN + THEN
        assertThrows(ReservationNotExistsException.class,
                () -> vehicle.rent(UUID.randomUUID()));
    }

    @Test
    void shouldThrowException_whenStatusCannotBeChangedToRented() {
        // given
        Rental rental = new Rental(UUID.randomUUID(),
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID,
                RentalStatus.RETURNED);

        Vehicle vehicle = vehicleWithRental(rental);

        // WHEN + THEN
        assertThrows(RentalStatusCanNotBeChangedException.class,
                () -> vehicle.rent(rental.getRentalId()));
        assertEquals(RentalStatus.RETURNED, rental.getStatus());
    }

    @Test
    void shouldChangeStatusToRented() {
        // GIVEN
        Rental rental = new Rental(UUID.randomUUID(),
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID,
                RentalStatus.RESERVED);

        Vehicle vehicle = vehicleWithRental(rental);

        // WHEN
        vehicle.rent(rental.getRentalId());

        // THEN
        assertEquals(RentalStatus.RENTED, rental.getStatus());
    }

    @Test
    void shouldThrowException_whenCancelAndReservationNotExists() {
        // GIVEN
        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN);

        // WHEN + THEN
        assertThrows(ReservationNotExistsException.class,
                () -> vehicle.cancel(UUID.randomUUID()));
    }

    @Test
    void shouldThrowException_whenStatusCannotBeChangedToCancelled() {
        // GIVEN
        Rental rental = new Rental(UUID.randomUUID(),
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID,
                RentalStatus.RETURNED);

        Vehicle vehicle = vehicleWithRental(rental);

        // WHEN + THEN
        assertThrows(RentalStatusCanNotBeChangedException.class,
                () -> vehicle.cancel(rental.getRentalId()));
        assertEquals(RentalStatus.RETURNED, rental.getStatus());
    }

    @Test
    void shouldCancelReservationAndRemoveFromSchedule() {
        // GIVEN
        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T12:00:00Z");

        Rental rental = new Rental(UUID.randomUUID(), start, end, USER_ID, RentalStatus.RESERVED);
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(start, end);

        Map<UUID, Rental> rentals = new HashMap<>();
        rentals.put(rental.getRentalId(), rental);

        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());

        // WHEN
        vehicle.cancel(rental.getRentalId());

        // THEN
        assertEquals(RentalStatus.CANCELLED, rental.getStatus());
        assertFalse(schedule.containsKey(start));
    }

    @Test
    void shouldThrowException_whenReturnAndReservationNotExists() {
        // GIVEN
        Vehicle vehicle = new Vehicle("V1", VehicleType.SEDAN);

        // WHEN + THEN
        assertThrows(ReservationNotExistsException.class,
                () -> vehicle.returnCar(UUID.randomUUID()));
    }

    @Test
    void shouldThrowException_whenStatusCannotBeChangedToReturned() {
        // GIVEN
        Rental rental = new Rental(UUID.randomUUID(),
                t("2025-01-01T10:00:00Z"),
                t("2025-01-01T12:00:00Z"),
                USER_ID,
                RentalStatus.CANCELLED);

        Vehicle vehicle = vehicleWithRental(rental);

        // WHEN + THEN
        assertThrows(RentalStatusCanNotBeChangedException.class,
                () -> vehicle.returnCar(rental.getRentalId()));
        assertEquals(RentalStatus.CANCELLED, rental.getStatus());
    }

    @Test
    void shouldReturnCarSuccessfully() {
        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T12:00:00Z");

        Rental rental = new Rental(UUID.randomUUID(), start, end, USER_ID, RentalStatus.RENTED);

        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(start, end);

        Map<UUID, Rental> rentals = new HashMap<>();
        rentals.put(rental.getRentalId(), rental);

        Vehicle vehicle = new Vehicle(
                "V1",
                VehicleType.SEDAN,
                schedule,
                rentals,
                fixedClock
        );

        // WHEN
        vehicle.returnCar(rental.getRentalId());

        // THEN
        assertEquals(RentalStatus.RETURNED, rental.getStatus());
        assertFalse(schedule.containsKey(start));
    }

    @Test
    void shouldThrowException_whenReturnedAfterTime() {
        // GIVEN
        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T11:00:00Z");

        Rental rental = new Rental(UUID.randomUUID(), start, end, USER_ID, RentalStatus.RENTED);

        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(start, end);

        Map<UUID, Rental> rentals = new HashMap<>();
        rentals.put(rental.getRentalId(), rental);

        Vehicle vehicle = new Vehicle(
                "V1",
                VehicleType.SEDAN,
                schedule,
                rentals,
                fixedClock
        );

        // WHEN + THEN
        assertThrows(CarReturnedAfterTimeException.class,
                () -> vehicle.returnCar(rental.getRentalId()));
        assertEquals(RentalStatus.RETURNED, rental.getStatus());
        assertFalse(schedule.containsKey(start));
    }
    @Test
    void shouldHandleFullRentalFlow() {
        // GIVEN
        Vehicle vehicle = new Vehicle(
                "V1",
                VehicleType.SEDAN,
                new TreeMap<>(),
                new HashMap<>(),
                fixedClock
        );

        Instant start = t("2025-01-01T10:00:00Z");
        Instant end = t("2025-01-01T12:00:00Z");

        // WHEN
        Rental rental = vehicle.reserve(start, end, USER_ID).get();
        vehicle.rent(rental.getRentalId());
        vehicle.returnCar(rental.getRentalId());

        // THEN
        assertEquals(RentalStatus.RETURNED, rental.getStatus());
    }

    private Vehicle vehicleWithExistingRental(Instant start, Instant end) {
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(start, end);

        Rental rental = new Rental(UUID.randomUUID(), start, end, USER_ID, RentalStatus.RESERVED);
        Map<UUID, Rental> rentals = new HashMap<>();
        rentals.put(rental.getRentalId(), rental);

        return new Vehicle("V1", VehicleType.SEDAN, schedule, rentals,fixedClock);
    }


    private Vehicle vehicleWithRental(Rental rental) {
        TreeMap<Instant, Instant> schedule = new TreeMap<>();
        schedule.put(rental.getStartDate(), rental.getEndDate());

        Map<UUID, Rental> rentals = new HashMap<>();
        rentals.put(rental.getRentalId(), rental);

        return new Vehicle("V1", VehicleType.SEDAN, schedule, rentals, Clock.systemUTC());
    }

    private Instant t(String time) {
        return Instant.parse(time);
    }

    private Vehicle emptyVehicle() {
        return new Vehicle("V1", VehicleType.SEDAN, new TreeMap<>(), new HashMap<>(),fixedClock);
    }

    private TreeMap<Instant, Instant> copySchedule(TreeMap<Instant, Instant> original) {
        return new TreeMap<>(original);
    }

    private Map<UUID, Rental> copyRentals(Map<UUID, Rental> original) {
        return new HashMap<>(original);
    }

}