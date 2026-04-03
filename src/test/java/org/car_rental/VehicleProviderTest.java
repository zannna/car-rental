package org.car_rental;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class VehicleProviderTest {

    @Test
    void shouldReturnAllVehiclesForGivenType() {
        // given
        Vehicle v1 = new Vehicle("1", VehicleType.SEDAN);
        Vehicle v2 = new Vehicle("2", VehicleType.SEDAN);
        Vehicle v3 = new Vehicle("3", VehicleType.SEDAN);

        Map<VehicleType, List<Vehicle>> map = new HashMap<>();
        map.put(VehicleType.SEDAN, List.of(v1, v2, v3));

        VehicleProvider provider = new VehicleProvider(map);

        // when
        List<Vehicle> result = provider.findVehiclesFortType(VehicleType.SEDAN);

        // then
        assertEquals(3, result.size());
        assertTrue(result.containsAll(List.of(v1, v2, v3)));
    }

    @Test
    void shouldReturnNewListAndNotModifyOriginal() {
        // given
        Vehicle v1 = new Vehicle("1", VehicleType.SEDAN);
        List<Vehicle> originalList = new ArrayList<>(List.of(v1));

        Map<VehicleType, List<Vehicle>> map = new HashMap<>();
        map.put(VehicleType.SEDAN, originalList);

        VehicleProvider provider = new VehicleProvider(map);

        // when
        List<Vehicle> result = provider.findVehiclesFortType(VehicleType.SEDAN);

        result.add(new Vehicle("extra", VehicleType.SEDAN));

        // then
        assertEquals(1, originalList.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoVehiclesForType() {
        // given
        Map<VehicleType, List<Vehicle>> map = new HashMap<>();
        map.put(VehicleType.SUV, List.of(new Vehicle("1", VehicleType.SUV)));

        VehicleProvider provider = new VehicleProvider(map);

        // when
        List<Vehicle> result =  provider.findVehiclesFortType(VehicleType.SEDAN);

        //then
        assertTrue(result.isEmpty());
    }

}