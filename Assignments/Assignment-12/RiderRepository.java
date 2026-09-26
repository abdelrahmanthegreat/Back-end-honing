package masr;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class RiderRepository extends Repository<Rider> {

    public RiderRepository() {
        super("rider", Rider::id);
    }

    public void updateDashboard(String riderId, Order order) {
        if (riderId == null || order == null) {
            return;
        }
        find(riderId).ifPresent(rider -> rider.setDashboardStatus(
                "order " + order.id() + " is " + order.status().label()
                        + " for " + order.total() + " to " + order.address().shortLine()));
    }

    public List<Rider> onDuty() {
        return findAll(Rider::isOnDuty);
    }

    public List<Rider> free() {
        return findAll(rider -> rider.isOnDuty() && !rider.isBusy());
    }

    public Stream<Rider> eligibleFor(Order order, Restaurant restaurant, GeoService geo) {
        return free().stream().filter(rider -> rider.canHandle(order, restaurant, geo));
    }

    public List<Rider> byDeliveries() {
        return stream().sorted(Comparator.comparingInt(Rider::completedDeliveries).reversed()
                .thenComparing(Rider::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }
}
