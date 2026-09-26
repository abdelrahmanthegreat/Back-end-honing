package masr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ReportService {

    private final RestaurantRepository restaurants;
    private final CustomerRepository customers;
    private final RiderRepository riders;
    private final OrderRepository orders;
    private final LocalDateTime clock;

    public ReportService(RestaurantRepository restaurants, CustomerRepository customers,
                         RiderRepository riders, OrderRepository orders) {
        this(restaurants, customers, riders, orders, LocalDateTime.now());
    }

    public ReportService(RestaurantRepository restaurants, CustomerRepository customers,
                         RiderRepository riders, OrderRepository orders, LocalDateTime clock) {
        this.restaurants = restaurants;
        this.customers = customers;
        this.riders = riders;
        this.orders = orders;
        this.clock = clock;
    }

    public Money totalRevenue(LocalDate from, LocalDate to) {
        return orders.deliveredBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream()
                .map(Order::total)
                .reduce(Money.ZERO, Money::plus);
    }

    public List<RestaurantRevenue> topRestaurantsByRevenue(YearMonth month) {
        return orders.stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .filter(order -> YearMonth.from(order.deliveredAt() == null ? order.placedAt() : order.deliveredAt()).equals(month))
                .collect(Collectors.groupingBy(order -> order.restaurant().id()))
                .entrySet()
                .stream()
                .map(entry -> new RestaurantRevenue(
                        restaurants.find(entry.getKey()).map(Restaurant::name).orElse(entry.getKey()),
                        entry.getKey(),
                        entry.getValue().stream().map(Order::total).reduce(Money.ZERO, Money::plus),
                        entry.getValue().size()))
                .sorted(Comparator.comparing(RestaurantRevenue::revenue).reversed()
                        .thenComparing(RestaurantRevenue::restaurantName, String.CASE_INSENSITIVE_ORDER))
                .limit(5)
                .toList();
    }

    public List<DistrictAverage> averageOrderValuePerDistrict() {
        return orders.inStatus(OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(order -> order.address().district()))
                .entrySet()
                .stream()
                .map(entry -> new DistrictAverage(
                        entry.getKey(),
                        entry.getValue().size(),
                        average(entry.getValue().stream().map(Order::total).reduce(Money.ZERO, Money::plus),
                                entry.getValue().size())))
                .sorted(Comparator.comparing(DistrictAverage::district))
                .toList();
    }

    public List<RestaurantStanding> highlyRatedWithEnoughOrders(double minimumRating, int minimumCompletedOrders) {
        Map<String, Long> completedPerRestaurant = orders.inStatus(OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(order -> order.restaurant().id(), Collectors.counting()));
        return restaurants.stream()
                .filter(restaurant -> restaurant.rating() > minimumRating)
                .filter(restaurant -> completedPerRestaurant.getOrDefault(restaurant.id(), 0L) >= minimumCompletedOrders)
                .map(restaurant -> new RestaurantStanding(restaurant.name(), restaurant.id(), restaurant.rating(),
                        completedPerRestaurant.getOrDefault(restaurant.id(), 0L), restaurant.district()))
                .sorted(Comparator.comparingDouble(RestaurantStanding::rating).reversed()
                        .thenComparing(RestaurantStanding::restaurantName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Map<OrderStatus, Long> orderCountByStatus() {
        Map<OrderStatus, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(Order::status, () -> new java.util.EnumMap<>(OrderStatus.class), Collectors.counting()));
        Map<OrderStatus, Long> complete = new java.util.EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            complete.put(status, counts.getOrDefault(status, 0L));
        }
        return complete;
    }

    public List<RiderPerformance> riderPerformance() {
        Map<String, List<Order>> deliveredByRider = orders.stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED && order.riderId() != null)
                .collect(Collectors.groupingBy(Order::riderId));
        return riders.stream()
                .map(rider -> {
                    List<Order> delivered = deliveredByRider.getOrDefault(rider.id(), List.of());
                    Optional<Duration> average = averageDuration(delivered);
                    return new RiderPerformance(rider.id(), rider.name(), rider.vehicleType(),
                            rider.completedDeliveries(), delivered.size(), average,
                            average.map(Duration::toMinutes).orElse(0L),
                            totalRiderRevenue(delivered));
                })
                .sorted(Comparator.comparingInt(RiderPerformance::platformCompletedDeliveries).reversed()
                        .thenComparing(RiderPerformance::riderName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public MostOrderedItem mostFrequentlyOrderedItem() {
        Optional<Map.Entry<MenuItemKey, Long>> winner = orders.stream()
                .flatMap(order -> order.lines().stream())
                .collect(Collectors.groupingBy(line -> new MenuItemKey(line.item().ownerRestaurantId(), line.item().id()),
                        Collectors.summingLong(OrderLine::units)))
                .entrySet()
                .stream()
                .max(Comparator.<Map.Entry<MenuItemKey, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(entry -> entry.getKey().itemId()));
        if (winner.isEmpty()) {
            return new MostOrderedItem(Optional.empty(), 0L, 0L);
        }
        Map.Entry<MenuItemKey, Long> best = winner.get();
        long distinctOrders = orders.stream()
                .filter(order -> order.lines().stream()
                        .anyMatch(line -> line.item().id().equals(best.getKey().itemId())
                                && line.item().ownerRestaurantId().equals(best.getKey().restaurantId())))
                .count();
        String name = restaurants.find(best.getKey().restaurantId())
                .flatMap(restaurant -> restaurant.findItem(best.getKey().itemId()))
                .map(MenuItem::name)
                .orElse(best.getKey().itemId());
        return new MostOrderedItem(Optional.of(new MenuItemKey(best.getKey().restaurantId(), best.getKey().itemId(), name)),
                best.getValue(), distinctOrders);
    }

    public CustomerHistory customerHistory(String customerId) {
        List<Order> history = orders.forCustomer(customerId).stream()
                .sorted(OrderRepository.NEWEST_FIRST)
                .toList();
        Money lifetimeSpent = history.stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED || order.isPaid())
                .map(Order::total)
                .reduce(Money.ZERO, Money::plus);
        return new CustomerHistory(customerId,
                customers.find(customerId).map(Customer::name).orElse(customerId),
                history, lifetimeSpent, history.size());
    }

    public Optional<PeakHour> peakOrderingHour() {
        Map<Integer, Long> perHour = orders.stream()
                .collect(Collectors.groupingBy(order -> order.placedAt().getHour(), Collectors.counting()));
        return perHour.entrySet()
                .stream()
                .max(Comparator.<Map.Entry<Integer, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(entry -> -entry.getKey()))
                .map(entry -> new PeakHour(entry.getKey(), entry.getValue(), perHour.size()));
    }

    public List<CustomerInactivity> customersNotOrderedRecently(int days) {
        LocalDate cutoff = clock.toLocalDate().minusDays(days);
        Map<String, LocalDate> lastOrderByCustomer = orders.stream()
                .collect(Collectors.toMap(order -> order.customer().id(), Order::placedDate, (left, right) -> left.isAfter(right) ? left : right));
        return customers.stream()
                .map(customer -> {
                    LocalDate last = lastOrderByCustomer.get(customer.id());
                    return new CustomerInactivity(customer.id(), customer.name(), customer.mobile(),
                            last, last == null ? null : java.time.temporal.ChronoUnit.DAYS.between(last, clock.toLocalDate()),
                            last == null || last.isBefore(cutoff));
                })
                .filter(CustomerInactivity::outsideWindow)
                .sorted(Comparator.comparing(CustomerInactivity::lastOrderDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CustomerInactivity::customerName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public PlatformSnapshot platformSnapshot() {
        Map<OrderStatus, Long> byStatus = orderCountByStatus();
        long deliveredCount = orders.countByStatus(OrderStatus.DELIVERED);
        BigDecimal averageTicket = average(
                orders.inStatus(OrderStatus.DELIVERED).map(Order::total).reduce(Money.ZERO, Money::plus),
                deliveredCount);
        return new PlatformSnapshot(
                restaurants.size(),
                restaurants.openOnly().size(),
                customers.size(),
                riders.size(),
                riders.onDuty().size(),
                orders.size(),
                byStatus,
                orders.inStatus(OrderStatus.DELIVERED).map(Order::total).reduce(Money.ZERO, Money::plus),
                averageTicket,
                orders.countByStatus(OrderStatus.CANCELLED),
                dispatchBacklog());
    }

    public long dispatchBacklog() {
        return orders.countByStatus(OrderStatus.READY);
    }

    public List<MenuItemSales> topSellingItems(int limit) {
        return orders.stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .flatMap(order -> order.lines().stream())
                .collect(Collectors.groupingBy(line -> new MenuItemKey(line.item().ownerRestaurantId(), line.item().id(), line.item().name()),
                        Collectors.summingLong(OrderLine::units)))
                .entrySet()
                .stream()
                .map(entry -> new MenuItemSales(entry.getKey().itemName(), entry.getKey().restaurantId(), entry.getValue()))
                .sorted(Comparator.comparingLong(MenuItemSales::units).reversed()
                        .thenComparing(MenuItemSales::itemName, String.CASE_INSENSITIVE_ORDER))
                .limit(limit)
                .toList();
    }

    private Money totalRiderRevenue(List<Order> delivered) {
        return delivered.stream().map(Order::total).reduce(Money.ZERO, Money::plus);
    }

    private Optional<Duration> averageDuration(List<Order> delivered) {
        List<Duration> durations = delivered.stream().map(Order::deliveryDuration).flatMap(Optional::stream).toList();
        if (durations.isEmpty()) {
            return Optional.empty();
        }
        long seconds = durations.stream().mapToLong(Duration::toSeconds).sum() / durations.size();
        return Optional.of(Duration.ofSeconds(seconds));
    }

    private static BigDecimal average(Money total, long count) {
        if (count <= 0L) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return total.toBigDecimal().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    public record MenuItemKey(String restaurantId, String itemId, String itemName) {

        public MenuItemKey(String restaurantId, String itemId) {
            this(restaurantId, itemId, null);
        }
    }

    public record RestaurantRevenue(String restaurantName, String restaurantId, Money revenue, int deliveredOrders) {
    }

    public record DistrictAverage(District district, int deliveredOrders, BigDecimal averageOrderValue) {

        public String describe() {
            return district.label() + " | " + deliveredOrders + " orders | average "
                    + averageOrderValue.toPlainString() + " EGP";
        }
    }

    public record RestaurantStanding(String restaurantName, String restaurantId, double rating,
                                     long completedOrders, District district) {

        public String describe() {
            return restaurantName + " | rated " + String.format("%.1f", rating) + "/5 | "
                    + completedOrders + " completed orders | " + district.label();
        }
    }

    public record RiderPerformance(String riderId, String riderName, VehicleType vehicle,
                                   int platformCompletedDeliveries, int measuredDeliveries,
                                   Optional<Duration> averageDeliveryDuration, long averageMinutes,
                                   Money revenue) {

        public String describe() {
            return riderName + " (" + riderId + ") | " + vehicle.label() + " | "
                    + platformCompletedDeliveries + " deliveries | average duration "
                    + (averageDeliveryDuration.isEmpty() ? "no deliveries yet"
                    : Order.formatDuration(averageDeliveryDuration.get()))
                    + " | revenue " + revenue;
        }
    }

    public record MostOrderedItem(Optional<MenuItemKey> item, long totalUnits, long ordersContainingIt) {

        public String describe() {
            return item.map(key -> key.itemName() + " (" + key.itemId() + " at " + key.restaurantId() + "): "
                    + totalUnits + " units across " + ordersContainingIt + " order(s)").orElse("no menu item has ever been ordered");
        }
    }

    public record CustomerHistory(String customerId, String customerName, List<Order> orders, Money lifetimeSpent, int orderCount) {

        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append(customerName).append(" (").append(customerId).append(") - ")
                    .append(orderCount).append(" order(s), lifetime spend ").append(lifetimeSpent).append('\n');
            for (Order order : orders) {
                sb.append("  ").append(order.placedAt()).append(" | ").append(order.id())
                        .append(" | ").append(order.status().label())
                        .append(" | ").append(order.total())
                        .append(" | ").append(order.restaurant().name())
                        .append(order.isPaid() ? " | paid" : " | unpaid")
                        .append('\n');
            }
            return sb.toString();
        }
    }

    public record PeakHour(int hour, long orders, int distinctHoursSeen) {

        public String describe() {
            return String.format("%02d:00 - %02d:59 with %d order(s) across %d active hour(s)", hour, hour, orders, distinctHoursSeen);
        }
    }

    public record CustomerInactivity(String customerId, String customerName, String mobile,
                                     LocalDate lastOrderDate, Long daysSinceLastOrder, boolean outsideWindow) {

        public String describe() {
            if (lastOrderDate == null) {
                return customerName + " (" + customerId + ") | never ordered";
            }
            return customerName + " (" + customerId + ") | last ordered " + lastOrderDate + " | "
                    + daysSinceLastOrder + " day(s) ago";
        }
    }

    public record PlatformSnapshot(int restaurants, int openRestaurants, int customers, int riders,
                                   int ridersOnDuty, int orders, Map<OrderStatus, Long> ordersByStatus,
                                   Money totalRevenue, BigDecimal averageTicket, long cancelledOrders,
                                   long ordersWaitingForRider) {

        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append("restaurants      : ").append(restaurants).append(" (").append(openRestaurants).append(" open)\n");
            sb.append("customers       : ").append(customers).append('\n');
            sb.append("riders          : ").append(riders).append(" (").append(ridersOnDuty).append(" on duty)\n");
            sb.append("orders          : ").append(orders).append('\n');
            sb.append("waiting rider   : ").append(ordersWaitingForRider).append('\n');
            sb.append("cancelled       : ").append(cancelledOrders).append('\n');
            sb.append("lifetime revenue: ").append(totalRevenue).append('\n');
            sb.append("average ticket  : ").append(averageTicket.toPlainString()).append(" EGP\n");
            sb.append("by status       : ").append(ordersByStatus).append('\n');
            return sb.toString();
        }
    }

    public record MenuItemSales(String itemName, String restaurantId, long units) {
    }
}
