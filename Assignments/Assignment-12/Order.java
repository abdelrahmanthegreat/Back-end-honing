package masr;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Order {

    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address address;
    private final Set<OrderLine> lines;
    private final LocalDateTime placedAt;
    private final String notes;
    private final String promotionCode;
    private final PriceBreakdown pricing;
    private final List<StatusChange> history;
    private final List<OrderEventListener> listeners;
    private OrderStatus status;
    private Rider rider;
    private LocalDateTime acceptedAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private boolean paid;
    private Money paidAmount;

    private Order(Builder builder) throws PlatformException {
        if (builder.id == null || builder.id.isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "an order id is required");
        }
        if (builder.customer == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "an order must belong to a customer");
        }
        if (builder.restaurant == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "an order must belong to a restaurant");
        }
        if (builder.address == null) {
            throw new ValidationException(ValidationException.ADDRESS_NOT_OWNED, "a delivery address is required");
        }
        this.id = builder.id.trim();
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.address = builder.address;
        this.placedAt = builder.placedAt == null ? LocalDateTime.now() : builder.placedAt;
        this.notes = builder.notes == null ? "" : builder.notes.trim();
        this.promotionCode = builder.promotion == null ? null : builder.promotion.code();
        this.paidAmount = Money.ZERO;
        if (builder.lines == null || builder.lines.isEmpty()) {
            throw new ValidationException(ValidationException.EMPTY_ORDER, "an order must contain at least one line item");
        }
        Set<OrderLine> unique = new LinkedHashSet<>(builder.lines);
        if (unique.size() != builder.lines.size()) {
            throw new ValidationException(ValidationException.INVALID_QUANTITY,
                    "the same menu item appears more than once in this order - combine the quantities instead");
        }
        this.lines = java.util.Collections.unmodifiableSet(unique);
        for (OrderLine line : this.lines) {
            line.item().validateQuantity(line.quantity());
        }
        this.pricing = Objects.requireNonNull(builder.engine, "a pricing engine is required")
                .price(customer, restaurant, address, this.lines, builder.promotion, placedAt.toLocalDate());
        this.status = OrderStatus.PLACED;
        this.history = new ArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
        builder.listeners.forEach(this.listeners::add);
        this.history.add(new StatusChange(null, OrderStatus.PLACED, placedAt, "order placed"));
        this.fire(null, OrderStatus.PLACED, "order placed for " + pricing.oneLine());
    }

    public static Builder builder(PricingEngine engine) {
        return new Builder(engine);
    }

    public String id() {
        return id;
    }

    public Customer customer() {
        return customer;
    }

    public Restaurant restaurant() {
        return restaurant;
    }

    public Address address() {
        return address;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public LocalDateTime placedAt() {
        return placedAt;
    }

    public LocalDate placedDate() {
        return placedAt.toLocalDate();
    }

    public String notes() {
        return notes;
    }

    public String promotionCode() {
        return promotionCode;
    }

    public PriceBreakdown pricing() {
        return pricing;
    }

    public Money total() {
        return pricing.total();
    }

    public OrderStatus status() {
        return status;
    }

    public Optional<Rider> rider() {
        return Optional.ofNullable(rider);
    }

    public String riderId() {
        return rider == null ? null : rider.id();
    }

    public boolean isPaid() {
        return paid;
    }

    public Money paidAmount() {
        return paidAmount;
    }

    public String cancellationReason() {
        return cancellationReason;
    }

    public int totalUnits() {
        return lines.stream().mapToInt(OrderLine::units).sum();
    }

    public List<StatusChange> history() {
        return List.copyOf(history);
    }

    public void addListener(OrderEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(OrderEventListener listener) {
        listeners.remove(listener);
    }

    public List<String> listenerNames() {
        return listeners.stream().map(OrderEventListener::name).toList();
    }

    public Duration elapsedSincePlacement() {
        return Duration.between(placedAt, LocalDateTime.now());
    }

    public Optional<Duration> deliveryDuration() {
        if (pickedUpAt == null || deliveredAt == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(pickedUpAt, deliveredAt));
    }

    public boolean isActive() {
        return status.isActive();
    }

    public void transitionTo(OrderStatus next, String detail) throws LifecycleException {
        if (next == null) {
            throw new LifecycleException(LifecycleException.ILLEGAL_TRANSITION, "a target status is required");
        }
        if (status == next) {
            throw new LifecycleException(LifecycleException.ILLEGAL_TRANSITION,
                    "order " + id + " is already " + next.label());
        }
        if (status == OrderStatus.CANCELLED) {
            throw new LifecycleException(LifecycleException.ALREADY_CANCELLED,
                    "order " + id + " was cancelled and a cancellation is final");
        }
        if (status == OrderStatus.DELIVERED) {
            throw new LifecycleException(LifecycleException.ALREADY_DELIVERED,
                    "order " + id + " is already delivered and cannot change status");
        }
        if (!status.canTransitionTo(next)) {
            throw new LifecycleException(LifecycleException.ILLEGAL_TRANSITION,
                    "order " + id + " cannot move from " + status.label() + " to " + next.label()
                            + " (allowed: " + describeAllowed() + ")");
        }
        OrderStatus previous = status;
        status = next;
        LocalDateTime now = LocalDateTime.now();
        history.add(new StatusChange(previous, next, now, detail));
        if (next == OrderStatus.ACCEPTED) {
            acceptedAt = now;
        } else if (next == OrderStatus.READY) {
            readyAt = now;
        } else if (next == OrderStatus.OUT_FOR_DELIVERY) {
            pickedUpAt = now;
        } else if (next == OrderStatus.DELIVERED) {
            deliveredAt = now;
        } else if (next == OrderStatus.CANCELLED) {
            cancelledAt = now;
            cancellationReason = detail;
        }
        fire(previous, next, detail);
    }

    void assignTo(Rider newRider) throws DispatchException, LifecycleException {
        if (newRider == null) {
            throw new DispatchException(DispatchException.NO_RIDER_AVAILABLE, "a rider is required");
        }
        if (rider != null) {
            throw new DispatchException(DispatchException.ALREADY_ASSIGNED,
                    "order " + id + " is already assigned to rider " + rider.id());
        }
        if (status != OrderStatus.READY) {
            throw new LifecycleException(LifecycleException.ORDER_NOT_READY,
                    "order " + id + " must be READY before a rider can be assigned, it is " + status.label());
        }
        newRider.assign(this);
        rider = newRider;
        transitionTo(OrderStatus.ASSIGNED, "assigned to rider " + newRider.id());
    }

    void markPaid(Money amount) {
        this.paid = true;
        this.paidAmount = amount;
    }

    void releaseRider() {
        if (rider != null) {
            rider.release(this);
            rider = null;
        }
    }

    public LocalDateTime acceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime readyAt() {
        return readyAt;
    }

    public LocalDateTime pickedUpAt() {
        return pickedUpAt;
    }

    public LocalDateTime deliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime cancelledAt() {
        return cancelledAt;
    }

    public String trackingLine() {
        return "order " + id + " | " + status.label() + " | placed " + placedAt
                + " | elapsed " + formatDuration(elapsedSincePlacement())
                + " | rider " + (riderId() == null ? "unassigned" : riderId())
                + " | total " + total();
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("ORDER ").append(id).append("  (").append(status.label()).append(")\n");
        sb.append("  customer : ").append(customer.name()).append(" (").append(customer.id()).append(")\n");
        sb.append("  restaurant: ").append(restaurant.name()).append(" - ").append(restaurant.district().label()).append('\n');
        sb.append("  address  : ").append(address.shortLine()).append('\n');
        sb.append("  placed   : ").append(placedAt).append("  (elapsed ").append(formatDuration(elapsedSincePlacement())).append(")\n");
        sb.append("  rider    : ").append(riderId() == null ? "unassigned" : riderId()).append('\n');
        if (!notes.isEmpty()) {
            sb.append("  notes    : ").append(notes).append('\n');
        }
        if (promotionCode != null) {
            sb.append("  promotion: ").append(promotionCode).append('\n');
        }
        sb.append("  payment : ").append(paid ? "paid " + paidAmount : "unpaid").append('\n');
        sb.append("  items   :\n");
        for (OrderLine line : lines) {
            sb.append("    - ").append(line.describe()).append('\n');
        }
        sb.append(pricing.describe().indent(3));
        return sb.toString();
    }

    private String describeAllowed() {
        List<String> labels = status.allowedTransitions().stream().map(OrderStatus::label).toList();
        return labels.isEmpty() ? "none, " + status.label() + " is final" : String.join(", ", labels);
    }

    private void fire(OrderStatus previous, OrderStatus next, String detail) {
        OrderEvent event = new OrderEvent(id, this, previous, next, LocalDateTime.now(), detail);
        for (OrderEventListener listener : listeners) {
            try {
                listener.onOrderEvent(event);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static String formatDuration(Duration duration) {
        long minutes = Math.max(0L, duration.toMinutes());
        if (minutes < 60L) {
            return minutes + " min";
        }
        return String.format(Locale.ROOT, "%dh %02dm", minutes / 60L, minutes % 60L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Order other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Order[" + id + ", " + status + ", " + total() + "]";
    }

    public record StatusChange(OrderStatus from, OrderStatus to, LocalDateTime at, String detail) {

        public String describe() {
            return (from == null ? "created" : from.label()) + " -> " + to.label() + " at " + at
                    + (detail == null || detail.isBlank() ? "" : " (" + detail + ")");
        }
    }

    public static final class Builder {

        private final PricingEngine engine;
        private final List<OrderLine> lines = new ArrayList<>();
        private final List<OrderEventListener> listeners = new ArrayList<>();
        private String id;
        private Customer customer;
        private Restaurant restaurant;
        private Address address;
        private Promotion promotion;
        private String notes;
        private LocalDateTime placedAt;

        private Builder(PricingEngine engine) {
            this.engine = engine;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder restaurant(Restaurant restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder promotion(Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Builder promotionCode(String code, PromotionRegistry registry) throws PromotionException {
            this.promotion = code == null || code.isBlank() ? null : registry.require(code);
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder placedAt(LocalDateTime placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Builder line(MenuItem item, java.math.BigDecimal quantity) {
            if (item != null) {
                lines.add(OrderLine.of(item, quantity));
            }
            return this;
        }

        public Builder listeners(List<OrderEventListener> orderListeners) {
            if (orderListeners != null) {
                this.listeners.addAll(orderListeners);
            }
            return this;
        }

        public Builder lines(List<OrderLine> orderLines) {
            if (orderLines != null) {
                lines.addAll(orderLines);
            }
            return this;
        }

        public int lineCount() {
            return lines.size();
        }

        public Order build() throws PlatformException {
            return new Order(this);
        }
    }
}
