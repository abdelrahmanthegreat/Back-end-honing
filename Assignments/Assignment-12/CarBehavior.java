package masr;

public final class CarBehavior implements RiderBehavior {

    @Override
    public VehicleType vehicleType() {
        return VehicleType.CAR;
    }

    @Override
    public double speedKmh() {
        return 30.0d;
    }

    @Override
    public double rangeKm() {
        return 40.0d;
    }

    @Override
    public int maxUnitsPerTrip() {
        return 60;
    }
}
