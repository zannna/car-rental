package org.car_rental;

import org.car_rental.dto.RentalConfirmationDto;
import org.car_rental.exception.CarReturnedAfterTimeException;
import org.car_rental.exception.NoAvailableCarsException;
import org.car_rental.exception.ReservationNotExistsException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RentalManager {

    private final Map<UUID, Vehicle> rentalIdToVehicle;
    private final VehicleProvider vehicleProvider;
    private final Clock clock;

    public RentalManager(Map<UUID, Vehicle> rentalIdToVehicle,
                         VehicleProvider vehicleProvider,
                         Clock clock) {
        this.rentalIdToVehicle = rentalIdToVehicle;
        this.vehicleProvider = vehicleProvider;
        this.clock = clock;
    }

    public RentalManager(Map<UUID, Vehicle> rentalIdToVehicle,
                         VehicleProvider vehicleProvider) {
        this(rentalIdToVehicle, vehicleProvider, Clock.systemUTC());
    }

    public RentalConfirmationDto reserve(String userId, VehicleType type, Instant startDate, int numberOfDays) {
        validateInput(userId, type, startDate, numberOfDays);

        Instant endDate = startDate.plus(numberOfDays, ChronoUnit.DAYS);
        List<Vehicle> vehicles = vehicleProvider.findVehiclesFortType(type);

        for (Vehicle v : vehicles) {
            Optional<Rental> optionalRental = v.reserve(startDate, endDate, userId);
            if (optionalRental.isPresent()) {
                Rental rental = optionalRental.get();
                rentalIdToVehicle.put(rental.getRentalId(), v);

                return new RentalConfirmationDto(
                        rental.getRentalId(),
                        v.getVehicleId(),
                        rental.getStartDate(),
                        rental.getEndDate()
                );
            }
        }

        throw new NoAvailableCarsException("No available cars for renting");
    }

    public void rentCar(UUID rentalId) {
        Vehicle vehicle = findVehicleByRentalId(rentalId);
        vehicle.rent(rentalId);
    }

    public void cancelReservation(UUID rentalId) {
        Vehicle vehicle = findVehicleByRentalId(rentalId);
        vehicle.cancel(rentalId);
        rentalIdToVehicle.remove(rentalId);
    }

    public void returnCar(UUID rentalId) {
        Vehicle vehicle = findVehicleByRentalId(rentalId);
        try {
            vehicle.returnCar(rentalId);
        } catch (CarReturnedAfterTimeException e) {
            rentalIdToVehicle.remove(rentalId);
            throw e;
        }
        rentalIdToVehicle.remove(rentalId);
    }

    private void validateInput(String userId, VehicleType type, Instant startDate, int numberOfDays) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }

        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        if (startDate == null) {
            throw new IllegalArgumentException("startDate cannot be null");
        }

        if (numberOfDays <= 0) {
            throw new IllegalArgumentException("numberOfDays must be greater than 0");
        }

        if (startDate.isBefore(clock.instant())) {
            throw new IllegalArgumentException("startDate cannot be in the past");
        }
    }

    private Vehicle findVehicleByRentalId(UUID rentalId) {
        Vehicle vehicle = rentalIdToVehicle.get(rentalId);
        if (vehicle == null) {
            throw new ReservationNotExistsException("Reservation not exist");
        }
        return vehicle;
    }
}