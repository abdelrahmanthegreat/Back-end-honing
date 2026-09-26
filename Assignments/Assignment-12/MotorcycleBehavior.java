package masr;

public final class MotorcycleBehavior implements RiderBehavior {

    @Override
    public VehicleType vehicleType() {
        return VehicleType.MOTORCYCLE;
    }

    @Override
    public double speedKmh() {
        return 45.0d;
    }

    @Override
    public double rangeKm() {
        return 25.0d;
    }

    @Override
    public int maxUnitsPerTrip() {
        return 30;
    }
}
