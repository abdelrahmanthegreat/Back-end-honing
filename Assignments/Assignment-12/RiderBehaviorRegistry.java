package masr;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RiderBehaviorRegistry {

    private static final Map<VehicleType, RiderBehavior> STRATEGIES = new EnumMap<>(VehicleType.class);

    static {
        register(new MotorcycleBehavior());
        register(new BicycleBehavior());
        register(new CarBehavior());
    }

    private RiderBehaviorRegistry() {
    }

    public static void register(RiderBehavior behavior) {
        STRATEGIES.put(behavior.vehicleType(), behavior);
    }

    public static RiderBehavior behaviorFor(VehicleType vehicleType) {
        RiderBehavior behavior = STRATEGIES.get(vehicleType);
        if (behavior == null) {
            throw new IllegalStateException("no dispatch behaviour registered for vehicle type " + vehicleType
                    + " - register one with RiderBehaviorRegistry.register(...)");
        }
        return behavior;
    }

    public static Map<VehicleType, RiderBehavior> all() {
        return new LinkedHashMap<>(STRATEGIES);
    }
}
