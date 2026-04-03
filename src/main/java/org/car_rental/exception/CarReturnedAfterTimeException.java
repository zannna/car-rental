package org.car_rental.exception;

public class CarReturnedAfterTimeException extends RuntimeException {
    public CarReturnedAfterTimeException(String message) {
        super(message);
    }
}
