package masr;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {

    PLACED("Placed"),
    ACCEPTED("Accepted"),
    PREPARING("Preparing"),
    READY("Ready"),
    ASSIGNED("Assigned"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = buildTransitions();

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> map = new EnumMap<>(OrderStatus.class);
        map.put(PLACED, EnumSet.of(ACCEPTED, CANCELLED));
        map.put(ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
        map.put(PREPARING, EnumSet.of(READY, CANCELLED));
        map.put(READY, EnumSet.of(ASSIGNED, CANCELLED));
        map.put(ASSIGNED, EnumSet.of(OUT_FOR_DELIVERY, CANCELLED));
        map.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
        map.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        map.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        Map<OrderStatus, Set<OrderStatus>> immutable = new EnumMap<>(OrderStatus.class);
        map.forEach((status, next) -> immutable.put(status, Collections.unmodifiableSet(EnumSet.copyOf(next))));
        return Collections.unmodifiableMap(immutable);
    }

    public Set<OrderStatus> allowedTransitions() {
        return ALLOWED.get(this);
    }

    public boolean canTransitionTo(OrderStatus next) {
        return next != null && allowedTransitions().contains(next);
    }

    public boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }

    public boolean isCancellable() {
        return allowedTransitions().contains(CANCELLED);
    }

    public boolean isActive() {
        return this != DELIVERED && this != CANCELLED;
    }

    public static String options() {
        StringBuilder sb = new StringBuilder();
        for (OrderStatus status : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(status.label());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return label;
    }
}
