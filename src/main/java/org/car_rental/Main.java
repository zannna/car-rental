package org.car_rental;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        Vehicle sedan1 = new Vehicle("SEDAN-1", VehicleType.SEDAN);
        Vehicle sedan2 = new Vehicle("SEDAN-2", VehicleType.SEDAN);
        Vehicle suv1 = new Vehicle("SUV-1", VehicleType.SUV);

        Map<VehicleType, List<Vehicle>> vehiclesMap = new HashMap<>();
        vehiclesMap.put(VehicleType.SEDAN, List.of(sedan1, sedan2));
        vehiclesMap.put(VehicleType.SUV, List.of(suv1));
        vehiclesMap.put(VehicleType.VAN, List.of());

        VehicleProvider vehicleProvider = new VehicleProvider(vehiclesMap);

        Map<UUID, Vehicle> rentalIdToVehicle = new HashMap<>();
        RentalManager rentalManager = new RentalManager(rentalIdToVehicle, vehicleProvider);

        Instant now = Instant.now();

            System.out.println("REZERWACJA");
            var confirmation = rentalManager.reserve(
                    "user1",
                    VehicleType.SEDAN,
                    now,
                    2
            );
            System.out.println("Zarezerwowano: " + confirmation);

            UUID rentalId = confirmation.rentalId();

            System.out.println("WYPOŻYCZENIE");
            rentalManager.rentCar(rentalId);
            System.out.println("Auto wypożyczone");

            System.out.println("ZWROT");
            rentalManager.returnCar(rentalId);
            System.out.println("Auto zwrócone");

        }
}