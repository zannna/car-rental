package org.car_rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VehicleProvider {
    private final Map<VehicleType, List<Vehicle>> vehiclesMap;

    public VehicleProvider(Map<VehicleType, List<Vehicle>> vehiclesMap) {
        this.vehiclesMap = vehiclesMap;
    }

    public List<Vehicle> findVehiclesFortType(VehicleType type) {
        List<Vehicle> typedVehicles = vehiclesMap.getOrDefault(type, List.of()).stream().toList();
        List<Vehicle> vehicles = new ArrayList<>(typedVehicles);
        Collections.shuffle(vehicles);
        return vehicles;
    }
}
