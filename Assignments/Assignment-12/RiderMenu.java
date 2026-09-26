package masr;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public final class RiderMenu {

    private final ConsoleIo io;
    private final MenuSupport menu;
    private final Platform platform;

    public RiderMenu(ConsoleIo io, MenuSupport menu) {
        this.io = io;
        this.menu = menu;
        this.platform = menu.platform();
    }

    public void loop() {
        while (true) {
            io.banner("MASR DELIVERY - Rider Area");
            io.println(" 1. Select a rider");
            io.println(" 2. Go on duty / off duty");
            io.println(" 3. View the assigned order");
            io.println(" 4. Mark the order picked up");
            io.println(" 5. Mark the order delivered");
            io.println(" 6. Personal delivery statistics");
            io.println(" 7. Move to another district");
            io.println(" 8. Dispatch the next ready order");
            io.println(" 0. Back to the main menu");
            int choice = io.readChoice("Choose an option: ", 9);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> menu.run("select", this::selectRider);
                case 2 -> toggleDuty();
                case 3 -> menu.run("assigned", this::viewAssigned);
                case 4 -> menu.run("pickup", () -> markPickedUp());
                case 5 -> menu.run("deliver", () -> markDelivered());
                case 6 -> statistics();
                case 7 -> move();
                case 8 -> menu.run("dispatch", this::dispatch);
                default -> io.warn("Unknown option.");
            }
        }
    }

    private void selectRider() {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        io.success("Working with " + rider.describe());
        io.info("dashboard: " + rider.dashboardStatus());
        rider.activeOrderId().ifPresent(active -> io.info("carrying order " + active));
    }

    private void toggleDuty() {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        menu.run("duty", () -> {
            if (rider.isOnDuty()) {
                rider.goOffDuty();
                io.success(rider.name() + " is now off duty.");
            } else {
                rider.goOnDuty();
                io.success(rider.name() + " is now on duty in " + rider.currentDistrict().label() + ".");
            }
        });
    }

    private void viewAssigned() throws PlatformException {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        String active = rider.activeOrderId().orElse(null);
        if (active == null) {
            io.warn(rider.name() + " is not carrying an order.");
            return;
        }
        Order order = platform.orders().require(active);
        io.println();
        io.println(order.describe());
        io.info("elapsed since placement: " + Order.formatDuration(order.elapsedSincePlacement()));
    }

    private void markPickedUp() throws PlatformException {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        String active = rider.activeOrderId().orElseThrow(() -> new DispatchException(
                DispatchException.NO_READY_ORDER, rider.name() + " is not carrying an order"));
        platform.orderService().markPickedUp(active);
        Order order = platform.orders().require(active);
        io.success("Order " + order.id() + " is now out for delivery.");
        io.info("Deliver to " + order.address().shortLine() + " for " + order.total() + ".");
    }

    private void markDelivered() throws PlatformException {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        String active = rider.activeOrderId().orElseThrow(() -> new DispatchException(
                DispatchException.NO_READY_ORDER, rider.name() + " is not carrying an order"));
        Order order = platform.orderService().markDelivered(active);
        io.success("Order " + order.id() + " delivered. Total deliveries for " + rider.name()
                + ": " + rider.completedDeliveries() + ".");
        if (!order.isPaid()) {
            io.warn("The customer has not paid for this order yet - they can pay from the customer area.");
        }
    }

    private void statistics() {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        List<Order> delivered = platform.orders().forRider(rider.id()).stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .toList();
        io.heading("RIDER STATISTICS - " + rider.name());
        io.info("vehicle: " + rider.vehicleType().label() + " (" + rider.behavior().describe() + ")");
        io.info("completed deliveries: " + rider.completedDeliveries());
        io.info("status: " + (rider.isOnDuty() ? "on duty" : "off duty") + " in " + rider.currentDistrict().label());
        if (delivered.isEmpty()) {
            io.warn("No completed deliveries yet, so there is no average delivery time.");
            return;
        }
        Money revenue = Money.ZERO;
        Duration total = Duration.ZERO;
        int measured = 0;
        for (Order order : delivered) {
            io.info(order.id() + " | delivered " + order.deliveredAt() + " | " + order.total()
                    + " | " + order.address().shortLine());
            revenue = revenue.plus(order.total());
            Optional<Duration> duration = order.deliveryDuration();
            if (duration.isPresent()) {
                total = total.plus(duration.get());
                measured++;
            }
        }
        io.println();
        io.success("delivered orders: " + delivered.size() + " | revenue " + revenue);
        if (measured > 0) {
            io.success("average delivery time: " + Order.formatDuration(total.dividedBy(measured)));
        }
    }

    private void move() {
        Rider rider = menu.pickRider();
        if (rider == null) {
            return;
        }
        io.println("Districts: " + District.options());
        District district = District.parse(io.readRequired("New district: "));
        if (district == null) {
            io.warn("Unknown district - the rider did not move.");
            return;
        }
        rider.moveTo(district);
        io.success(rider.name() + " is now in " + district.label() + ".");
    }

    private void dispatch() throws PlatformException {
        List<String> pending = platform.dispatchQueue().pendingLines();
        if (pending.isEmpty()) {
            io.info("The dispatch queue is empty.");
        } else {
            io.println();
            io.println("Orders waiting for a rider (Gold first, then longest waiting):");
            pending.forEach(line -> io.info(line));
        }
        OrderService.DispatchResult result = platform.orderService().dispatchNext();
        io.success(result.describe());
    }
}
