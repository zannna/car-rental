package org.car_rental;

import org.car_rental.exception.CarReturnedAfterTimeException;
import org.car_rental.exception.RentalStatusCanNotBeChangedException;
import org.car_rental.exception.ReservationNotExistsException;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class Vehicle {
    private final String vehicleId;
    private final VehicleType type;
    private final TreeMap<Instant, Instant> rentalSchedule;
    private final Map<UUID, Rental> rentals;
    private final Clock clock;

    public Vehicle(String vehicleId, VehicleType type, TreeMap<Instant, Instant> rentalSchedule, Map<UUID, Rental> rentals, Clock clock) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.rentalSchedule = rentalSchedule;
        this.rentals = rentals;
        this.clock = clock;
    }

    public Vehicle(String vehicleId, VehicleType type) {
        this(vehicleId, type, new TreeMap<>(), new HashMap<>(), Clock.systemUTC());
    }

    public Optional<Rental> reserve(Instant startDate, Instant endDate, String userId) {
        synchronized (rentalSchedule) {
            if (willBeAvailable(startDate, endDate)) {
                Rental rental = new Rental(UUID.randomUUID(), startDate, endDate, userId, RentalStatus.RESERVED);
                rentalSchedule.put(startDate, endDate);
                rentals.put(rental.getRentalId(), rental);
                return Optional.of(rental);
            } else {
                return Optional.empty();
            }
        }

    }

    public void rent(UUID reservationId) {
        Rental rental = findRental(reservationId);
        validateIfStatusCanBeChanged(rental.getStatus(), RentalStatus.RENTED);
        rental.setStatus(RentalStatus.RENTED);
    }

    public void returnCar(UUID reservationId) {
        Rental rental = findRental(reservationId);
        validateIfStatusCanBeChanged(rental.getStatus(), RentalStatus.RETURNED);
        rental.setStatus(RentalStatus.RETURNED);
        rentalSchedule.remove(rental.getStartDate());
        if (clock.instant().isAfter(rental.getEndDate())) {
            throw new CarReturnedAfterTimeException("Car returned after time");
        }
    }

    public void cancel(UUID reservationId) {
        Rental rental = findRental(reservationId);
        validateIfStatusCanBeChanged(rental.getStatus(), RentalStatus.CANCELLED);
        rental.setStatus(RentalStatus.CANCELLED);
        rentalSchedule.remove(rental.getStartDate());
    }


    private boolean willBeAvailable(Instant startDate, Instant endDate) {
        Map.Entry<Instant, Instant> prevRental = rentalSchedule.floorEntry(startDate);
        if (prevRental != null) {
            if (prevRental.getKey().equals(startDate)) {
                return false;
            }
            Instant prevEnd = prevRental.getValue();
            if (prevEnd.isAfter(startDate)) {
                return false;
            }
        }
        Instant nextStart= rentalSchedule.ceilingKey(startDate);
        return nextStart == null || !nextStart.isBefore(endDate);
    }

    private void validateIfStatusCanBeChanged(RentalStatus from, RentalStatus to) {
        if (!from.canTransitionTo(to)) {
            throw new RentalStatusCanNotBeChangedException("Can not change status from " + from + " to " + to);
        }
    }

    private Rental findRental(UUID reservationId) {
        if (rentals.containsKey(reservationId)) {
            return rentals.get(reservationId);
        } else {
            throw new ReservationNotExistsException("Reservation not exist");
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public TreeMap<Instant, Instant> getRentalSchedule() {
        return rentalSchedule;
    }

    public Map<UUID, Rental> getRentals() {
        return rentals;
    }
}