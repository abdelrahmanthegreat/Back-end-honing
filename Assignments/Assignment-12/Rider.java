package masr;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Rider {

    private final String id;
    private String name;
    private final VehicleType vehicleType;
    private final RiderBehavior behavior;
    private District currentDistrict;
    private boolean onDuty;
    private int completedDeliveries;
    private String activeOrderId;
    private volatile String dashboardStatus;
    private final List<String> dashboardLog = new CopyOnWriteArrayList<>();

    public Rider(String id, String name, VehicleType vehicleType, District currentDistrict) {
        this.id = requireText(id, "rider id");
        this.name = requireText(name, "rider name");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        this.behavior = RiderBehaviorRegistry.behaviorFor(vehicleType);
        this.currentDistrict = Objects.requireNonNull(currentDistrict, "currentDistrict must not be null");
        this.onDuty = false;
        this.dashboardStatus = "off duty";
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public VehicleType vehicleType() {
        return vehicleType;
    }

    public RiderBehavior behavior() {
        return behavior;
    }

    public District currentDistrict() {
        return currentDistrict;
    }

    public void moveTo(District district) {
        this.currentDistrict = Objects.requireNonNull(district, "district must not be null");
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    public void goOnDuty() throws DispatchException {
        if (onDuty) {
            return;
        }
        if (activeOrderId != null) {
            throw new DispatchException(DispatchException.RIDER_BUSY,
                    "rider " + id + " still has active order " + activeOrderId);
        }
        onDuty = true;
        setDashboardStatus("on duty in " + currentDistrict.label());
    }

    public void goOffDuty() throws DispatchException {
        if (activeOrderId != null) {
            throw new DispatchException(DispatchException.RIDER_BUSY,
                    "rider " + id + " cannot go off duty while carrying order " + activeOrderId);
        }
        onDuty = false;
        setDashboardStatus("off duty");
    }

    public int completedDeliveries() {
        return completedDeliveries;
    }

    public Optional<String> activeOrderId() {
        return Optional.ofNullable(activeOrderId);
    }

    public boolean isBusy() {
        return activeOrderId != null;
    }

    public void assign(Order order) throws DispatchException {
        if (order == null) {
            throw new DispatchException(DispatchException.NO_READY_ORDER, "there is no order to assign");
        }
        if (activeOrderId != null) {
            throw new DispatchException(DispatchException.RIDER_BUSY,
                    "rider " + id + " is already carrying order " + activeOrderId
                            + " - a rider may hold at most one active order");
        }
        if (!onDuty) {
            throw new DispatchException(DispatchException.RIDER_OFF_DUTY,
                    "rider " + id + " is off duty and cannot be assigned order " + order.id());
        }
        activeOrderId = order.id();
        setDashboardStatus("carrying order " + order.id() + " to " + order.address().district().label());
    }

    public void release(Order order) {
        if (order != null && order.id().equals(activeOrderId)) {
            activeOrderId = null;
        }
    }

    public void completeDelivery() {
        completedDeliveries++;
    }

    public boolean canHandle(Order order, Restaurant restaurant, GeoService geo) {
        return !isBusy() && onDuty && behavior.canHandle(order, geo, restaurant);
    }

    public void setDashboardStatus(String status) {
        this.dashboardStatus = status;
        dashboardLog.add(LocalDateTime.now() + " - " + status);
        while (dashboardLog.size() > 50) {
            dashboardLog.remove(0);
        }
    }

    public String dashboardStatus() {
        return dashboardStatus;
    }

    public List<String> dashboardLog() {
        return List.copyOf(new ArrayList<>(dashboardLog));
    }

    public String describe() {
        return name + " (" + id + ") - " + vehicleType.label() + " in " + currentDistrict.label()
                + " | " + (onDuty ? "ON DUTY" : "off duty")
                + " | " + completedDeliveries + " delivered"
                + (activeOrderId == null ? " | free" : " | carrying " + activeOrderId)
                + " | " + behavior.describe();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Rider other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
