package org.car_rental.exception;

public class ReservationNotExistsException extends RuntimeException {

    public ReservationNotExistsException(String message) {
        super(message);
    }
}
