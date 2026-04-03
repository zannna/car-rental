package org.car_rental.dto;

import java.time.Instant;
import java.util.UUID;

public record RentalConfirmationDto(UUID rentalId, String vehicleId, Instant startDate, Instant endDate){
}
