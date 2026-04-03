package org.car_rental.exception;

public class RentalStatusCanNotBeChangedException extends RuntimeException {

    public RentalStatusCanNotBeChangedException(String message) {
        super(message);
    }

}
