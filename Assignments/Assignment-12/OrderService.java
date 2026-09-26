package masr;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class OrderService {

    private final RestaurantRepository restaurants;
    private final CustomerRepository customers;
    private final RiderRepository riders;
    private final OrderRepository orders;
    private final PromotionRegistry promotions;
    private final PricingEngine pricing;
    private final GeoService geo;
    private final DispatchQueue dispatchQueue;
    private final List<OrderEventListener> listeners;
    private final IdGenerator idGenerator;
    private final int riderSearchRadiusKm;

    public OrderService(RestaurantRepository restaurants, CustomerRepository customers, RiderRepository riders,
                        OrderRepository orders, PromotionRegistry promotions, PricingEngine pricing,
                        GeoService geo, DispatchQueue dispatchQueue, List<OrderEventListener> listeners,
                        IdGenerator idGenerator) {
        this.restaurants = restaurants;
        this.customers = customers;
        this.riders = riders;
        this.orders = orders;
        this.promotions = promotions;
        this.pricing = pricing;
        this.geo = geo;
        this.dispatchQueue = dispatchQueue;
        this.listeners = List.copyOf(listeners);
        this.idGenerator = idGenerator;
        this.riderSearchRadiusKm = PlatformConfig.get().riderSearchRadiusKm();
    }

    public Order placeOrder(Customer customer, Restaurant restaurant, Address address,
                            List<OrderLine> lines, String promotionCode, String notes) throws PlatformException {
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(restaurant, "restaurant must not be null");
        if (address == null) {
            throw new ValidationException(ValidationException.ADDRESS_NOT_OWNED, "a delivery address is required");
        }
        if (!customer.owns(address)) {
            throw new ValidationException(ValidationException.ADDRESS_NOT_OWNED,
                    "address " + address.shortLine() + " does not belong to customer " + customer.id());
        }
        if (!restaurant.isOpen()) {
            throw new CatalogException(CatalogException.RESTAURANT_CLOSED,
                    restaurant.name() + " is closed and cannot accept orders");
        }
        if (lines == null || lines.isEmpty()) {
            throw new ValidationException(ValidationException.EMPTY_ORDER, "an order must contain at least one line item");
        }
        List<OrderLine> orderLines = List.copyOf(lines);
        for (OrderLine line : orderLines) {
            if (orderLines.indexOf(line) != orderLines.lastIndexOf(line)) {
                throw new ValidationException(ValidationException.INVALID_QUANTITY,
                        "'" + line.item().name() + "' appears more than once - combine the quantities into a single line");
            }
        }
        validateLines(restaurant, orderLines);
        Promotion promotion = promotionCode == null || promotionCode.isBlank() ? null : promotions.require(promotionCode);

        Order order = Order.builder(pricing)
                .id(idGenerator.next("ORD"))
                .customer(customer)
                .restaurant(restaurant)
                .address(address)
                .lines(orderLines)
                .promotion(promotion)
                .notes(notes)
                .listeners(listeners)
                .build();

        reserveStock(orderLines);
        orders.add(order);
        customer.markOrderPlaced();
        return order;
    }

    public Order placeOrder(Customer customer, Restaurant restaurant, Address address,
                            List<OrderLine> lines, String promotionCode) throws PlatformException {
        return placeOrder(customer, restaurant, address, lines, promotionCode, null);
    }

    public void payFromWallet(String orderId) throws PlatformException {
        Order order = orders.require(orderId);
        if (order.isPaid()) {
            throw new PaymentException(PaymentException.ALREADY_PAID, "order " + order.id() + " is already paid");
        }
        if (order.status() == OrderStatus.CANCELLED) {
            throw new PaymentException(PaymentException.ORDER_NOT_PAYABLE, "order " + order.id() + " was cancelled");
        }
        order.customer().debit(order.total());
        order.markPaid(order.total());
    }

    public void cancelOrder(String orderId, String reason) throws PlatformException {
        Order order = orders.require(orderId);
        order.transitionTo(OrderStatus.CANCELLED, reason == null ? "cancelled by the customer" : reason);
        releaseStock(order);
        if (order.isPaid()) {
            order.customer().credit(order.paidAmount());
        }
        order.releaseRider();
        dispatchQueue.remove(order.id());
        order.customer().markOrderCancelled();
    }

    public void restaurantAccept(String orderId) throws PlatformException {
        Order order = orders.require(orderId);
        order.transitionTo(OrderStatus.ACCEPTED, "accepted by " + order.restaurant().name());
    }

    public void restaurantReject(String orderId, String reason) throws PlatformException {
        Order order = orders.require(orderId);
        cancelOrder(order.id(), reason == null || reason.isBlank()
                ? "rejected by " + order.restaurant().name() : reason);
    }

    public void markPreparing(String orderId) throws PlatformException {
        orders.require(orderId).transitionTo(OrderStatus.PREPARING, "kitchen started");
    }

    public void markReady(String orderId) throws PlatformException {
        Order order = orders.require(orderId);
        order.transitionTo(OrderStatus.READY, "ready for pickup");
        dispatchQueue.offer(order, order.customer().tier(), order.readyAt() == null ? LocalDateTime.now() : order.readyAt());
    }

    public Order markPickedUp(String orderId) throws PlatformException {
        Order order = orders.require(orderId);
        if (order.rider().isEmpty()) {
            throw new LifecycleException(LifecycleException.RIDER_NOT_ASSIGNED,
                    "order " + order.id() + " has no rider assigned");
        }
        order.transitionTo(OrderStatus.OUT_FOR_DELIVERY, "picked up by rider " + order.riderId());
        return order;
    }

    public Order markDelivered(String orderId) throws PlatformException {
        Order order = orders.require(orderId);
        Rider rider = order.rider().orElseThrow(() -> new LifecycleException(LifecycleException.RIDER_NOT_ASSIGNED,
                "order " + order.id() + " has no rider assigned"));
        order.transitionTo(OrderStatus.DELIVERED, "delivered by rider " + rider.id());
        rider.completeDelivery();
        rider.release(order);
        dispatchQueue.remove(order.id());
        order.customer().markOrderCompleted();
        return order;
    }

    public DispatchResult dispatchNext() throws PlatformException {
        Order order = dispatchQueue.pollNext().orElseThrow(() -> new DispatchException(DispatchException.NO_READY_ORDER,
                "there is no ready order waiting for dispatch"));
        Restaurant restaurant = order.restaurant();
        Rider chosen = riders.eligibleFor(order, restaurant, geo)
                .filter(rider -> geo.distanceKm(rider.currentDistrict(), restaurant.district()) <= riderSearchRadiusKm)
                .min(Comparator.comparingInt((Rider rider) -> geo.distanceKm(rider.currentDistrict(), restaurant.district())))
                .orElseThrow(() -> new DispatchException(DispatchException.NO_RIDER_AVAILABLE,
                        "no on duty rider within " + riderSearchRadiusKm + " km of " + restaurant.name()
                                + " can take order " + order.id() + " (" + order.totalUnits() + " units, vehicle limits apply)"));
        try {
            order.assignTo(chosen);
        } catch (PlatformException e) {
            dispatchQueue.offer(order, order.customer().tier(), order.readyAt());
            throw e;
        }
        return new DispatchResult(order, chosen);
    }

    public DispatchResult assignToRider(String orderId, String riderId) throws PlatformException {
        Order order = orders.require(orderId);
        Rider rider = riders.require(riderId);
        Restaurant restaurant = order.restaurant();
        if (!rider.behavior().canHandle(order, geo, restaurant)) {
            throw new DispatchException(DispatchException.RIDER_INELIGIBLE,
                    "rider " + rider.id() + " on a " + rider.vehicleType().label() + " cannot serve this order ("
                            + order.totalUnits() + " units, "
                            + geo.distanceKm(restaurant.district(), order.address().district()) + " km) - "
                            + rider.behavior().describe());
        }
        order.assignTo(rider);
        dispatchQueue.remove(order.id());
        return new DispatchResult(order, rider);
    }

    public DispatchQueue dispatchQueue() {
        return dispatchQueue;
    }

    public Order requireOrder(String orderId) throws ValidationException {
        return orders.require(orderId);
    }

    public RestaurantRepository restaurants() {
        return restaurants;
    }

    public CustomerRepository customers() {
        return customers;
    }

    public RiderRepository riders() {
        return riders;
    }

    public OrderRepository orders() {
        return orders;
    }

    public PricingEngine pricing() {
        return pricing;
    }

    public GeoService geo() {
        return geo;
    }

    private void validateLines(Restaurant restaurant, List<OrderLine> lines) throws PlatformException {
        for (OrderLine line : lines) {
            MenuItem item = line.item();
            if (!item.ownerRestaurantId().equals(restaurant.id())) {
                throw new CatalogException(CatalogException.ITEM_NOT_ON_MENU,
                        "'" + item.name() + "' is not on the menu of " + restaurant.name());
            }
            if (restaurant.findItem(item.id()).orElse(null) != item) {
                throw new CatalogException(CatalogException.ITEM_NOT_ON_MENU,
                        "'" + item.name() + "' is no longer on the menu of " + restaurant.name());
            }
            if (!item.isAvailable()) {
                throw new CatalogException(CatalogException.ITEM_UNAVAILABLE,
                        "'" + item.name() + "' is currently unavailable");
            }
            item.validateQuantity(line.quantity());
            item.checkAvailable(line.quantity());
        }
    }

    private void reserveStock(List<OrderLine> lines) throws PlatformException {
        for (OrderLine line : lines) {
            line.item().reserve(line.quantity());
        }
    }

    private void releaseStock(Order order) {
        for (OrderLine line : order.lines()) {
            line.item().release(line.quantity());
        }
    }

    public record DispatchResult(Order order, Rider rider) {

        public String describe() {
            return "order " + order.id() + " assigned to rider " + rider.name()
                    + " (" + rider.vehicleType().label() + ") carrying " + order.total()
                    + " to " + order.address().shortLine();
        }
    }
}
