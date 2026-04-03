package org.car_rental;

public enum RentalStatus {
    RESERVED,
    RENTED,
    CANCELLED,
    RETURNED;

    public boolean canTransitionTo(RentalStatus newStatus) {
        return switch (this) {
            case RESERVED -> newStatus == RENTED || newStatus == CANCELLED;
            case RENTED -> newStatus == RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }
}
