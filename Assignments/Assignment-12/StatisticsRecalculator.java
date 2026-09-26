package masr;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class StatisticsRecalculator implements OrderEventListener {

    private final Map<OrderStatus, LongAdder> statusCounts = new EnumMap<>(OrderStatus.class);
    private final Map<String, LongAdder> revenueByRestaurant = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> revenueByDistrict = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> itemsSoldByRestaurant = new ConcurrentHashMap<>();
    private final AtomicLong eventsProcessed = new AtomicLong();
    private volatile Duration totalDeliveryTime = Duration.ZERO;
    private volatile long deliveredCount;

    public StatisticsRecalculator() {
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status, new LongAdder());
        }
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        eventsProcessed.incrementAndGet();
        statusCounts.get(event.newStatus()).increment();
        Order order = event.order();
        if (event.newStatus() == OrderStatus.DELIVERED) {
            revenueByRestaurant.merge(order.restaurant().id(), new LongAdder(), (existing, adder) -> {
                adder.add(order.total().piastres());
                return adder;
            });
            revenueByDistrict.merge(order.address().district().name(), new LongAdder(), (existing, adder) -> {
                adder.add(order.total().piastres());
                return adder;
            });
            itemsSoldByRestaurant.merge(order.restaurant().id(), new LongAdder(), (existing, adder) -> {
                adder.add(order.totalUnits());
                return adder;
            });
            order.deliveryDuration().ifPresent(duration -> {
                synchronized (this) {
                    totalDeliveryTime = totalDeliveryTime.plus(duration);
                    deliveredCount++;
                }
            });
        }
    }

    @Override
    public String name() {
        return "StatisticsRecalculator";
    }

    public long countOf(OrderStatus status) {
        return statusCounts.get(status).sum();
    }

    public Map<OrderStatus, Long> snapshot() {
        Map<OrderStatus, Long> copy = new EnumMap<>(OrderStatus.class);
        statusCounts.forEach((status, adder) -> copy.put(status, adder.sum()));
        return copy;
    }

    public long revenueForRestaurant(String restaurantId) {
        LongAdder adder = revenueByRestaurant.get(restaurantId);
        return adder == null ? 0L : adder.sum();
    }

    public long revenueForDistrict(District district) {
        LongAdder adder = revenueByDistrict.get(district.name());
        return adder == null ? 0L : adder.sum();
    }

    public long eventsProcessed() {
        return eventsProcessed.get();
    }

    public Optional<Duration> averageDeliveryTime() {
        synchronized (this) {
            return deliveredCount == 0L ? Optional.empty() : Optional.of(totalDeliveryTime.dividedBy(deliveredCount));
        }
    }
}
