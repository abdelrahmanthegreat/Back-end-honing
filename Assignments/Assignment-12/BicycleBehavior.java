package masr;

public final class BicycleBehavior implements RiderBehavior {

    @Override
    public VehicleType vehicleType() {
        return VehicleType.BICYCLE;
    }

    @Override
    public double speedKmh() {
        return 18.0d;
    }

    @Override
    public double rangeKm() {
        return 12.0d;
    }

    @Override
    public int maxUnitsPerTrip() {
        return 12;
    }
}
