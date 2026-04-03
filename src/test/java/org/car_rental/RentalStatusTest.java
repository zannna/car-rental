package org.car_rental;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RentalStatusTest {

    @ParameterizedTest(name = "Transition from {0} to {1} should be {2}")
    @MethodSource("provideStatusTransitions")
    void shouldValidateTransitions(RentalStatus current, RentalStatus next, boolean expected) {
        assertEquals(expected, current.canTransitionTo(next));
    }

    private static Stream<Arguments> provideStatusTransitions() {
        return Stream.of(
                arguments(RentalStatus.RESERVED, RentalStatus.RENTED, true),
                arguments(RentalStatus.RESERVED, RentalStatus.CANCELLED, true),
                arguments(RentalStatus.RESERVED, RentalStatus.RETURNED, false),
                arguments(RentalStatus.RESERVED, RentalStatus.RESERVED, false),

                arguments(RentalStatus.RENTED, RentalStatus.RETURNED, true),
                arguments(RentalStatus.RENTED, RentalStatus.RESERVED, false),
                arguments(RentalStatus.RENTED, RentalStatus.CANCELLED, false),
                arguments(RentalStatus.RENTED, RentalStatus.RENTED, false),

                arguments(RentalStatus.CANCELLED, RentalStatus.RESERVED, false),
                arguments(RentalStatus.CANCELLED, RentalStatus.RENTED, false),
                arguments(RentalStatus.CANCELLED, RentalStatus.RETURNED, false),
                arguments(RentalStatus.CANCELLED, RentalStatus.CANCELLED, false),

                arguments(RentalStatus.RETURNED, RentalStatus.RENTED, false),
                arguments(RentalStatus.RETURNED, RentalStatus.RESERVED, false),
                arguments(RentalStatus.RETURNED, RentalStatus.CANCELLED, false),
                arguments(RentalStatus.RETURNED, RentalStatus.RETURNED, false)
        );
    }
}