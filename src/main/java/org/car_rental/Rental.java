package org.car_rental;

import java.time.Instant;
import java.util.UUID;

public class Rental {
    private final UUID rentalId;
    private final Instant startDate;
    private final Instant endDate;
    private final String userId;
    private RentalStatus status;

    public Rental(UUID rentalId, Instant startDate,  Instant endDate, String userId, RentalStatus status) {
        this.rentalId = rentalId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.status = status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public UUID getRentalId() {
        return rentalId;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public String getUserId() {
        return userId;
    }
}