package masr;

public interface RiderBehavior {

    VehicleType vehicleType();

    double speedKmh();

    double rangeKm();

    int maxUnitsPerTrip();

    default boolean canCarry(int units) {
        return units <= maxUnitsPerTrip();
    }

    default boolean canServe(District from, District to, GeoService geo) {
        return geo.distanceKm(from, to) <= rangeKm();
    }

    default boolean canHandle(Order order, GeoService geo, Restaurant restaurant) {
        if (order == null || restaurant == null) {
            return false;
        }
        int units = order.totalUnits();
        Address address = order.address();
        return canCarry(units) && canServe(restaurant.district(), address.district(), geo);
    }

    default String describe() {
        return vehicleType().label() + ": range " + rangeKm() + " km, top speed " + speedKmh()
                + " km/h, up to " + maxUnitsPerTrip() + " units per trip";
    }
}
