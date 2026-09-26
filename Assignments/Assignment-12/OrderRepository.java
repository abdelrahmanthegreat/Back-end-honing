package masr;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class OrderRepository extends Repository<Order> {

    public OrderRepository() {
        super("order", Order::id);
    }

    public static final Comparator<Order> NEWEST_FIRST =
            Comparator.comparing(Order::placedAt).reversed().thenComparing(Order::id);

    public List<Order> byStatus(OrderStatus status) {
        return findAll(order -> order.status() == status);
    }

    public Stream<Order> inStatus(OrderStatus status) {
        return stream().filter(order -> order.status() == status);
    }

    public List<Order> forCustomer(String customerId) {
        return findAll(order -> order.customer().id().equals(customerId));
    }

    public List<Order> forRestaurant(String restaurantId) {
        return findAll(order -> order.restaurant().id().equals(restaurantId));
    }

    public List<Order> forRider(String riderId) {
        return findAll(order -> order.riderId() != null && order.riderId().equals(riderId));
    }

    public List<Order> newestFirst() {
        return stream().sorted(NEWEST_FIRST).toList();
    }

    public List<Order> placedBetween(LocalDateTime from, LocalDateTime to) {
        return findAll(order -> !order.placedAt().isBefore(from) && !order.placedAt().isAfter(to));
    }

    public List<Order> deliveredBetween(LocalDateTime from, LocalDateTime to) {
        return findAll(order -> order.status() == OrderStatus.DELIVERED
                && order.deliveredAt() != null
                && !order.deliveredAt().isBefore(from)
                && !order.deliveredAt().isAfter(to));
    }

    public List<Order> onDate(LocalDate date) {
        return findAll(order -> order.placedDate().equals(date));
    }

    public long countByStatus(OrderStatus status) {
        return inStatus(status).count();
    }
}
