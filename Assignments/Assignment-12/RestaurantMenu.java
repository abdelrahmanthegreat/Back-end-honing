package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class RestaurantMenu {

    private final ConsoleIo io;
    private final MenuSupport menu;
    private final Platform platform;

    public RestaurantMenu(ConsoleIo io, MenuSupport menu) {
        this.io = io;
        this.menu = menu;
        this.platform = menu.platform();
    }

    public void loop() {
        while (true) {
            io.banner("MASR DELIVERY - Restaurant Area");
            io.println(" 1. Select a restaurant");
            io.println(" 2. Open or close the restaurant");
            io.println(" 3. Today's orders and revenue");
            io.println(" 4. Accept a pending order");
            io.println(" 5. Reject a pending order");
            io.println(" 6. Mark an order as preparing");
            io.println(" 7. Mark an order as ready for pickup");
            io.println(" 8. Manage the menu");
            io.println(" 0. Back to the main menu");
            int choice = io.readChoice("Choose an option: ", 9);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> menu.run("select", this::selectRestaurant);
                case 2 -> toggleOpen();
                case 3 -> todayReport();
                case 4 -> changeStatus("ACCEPT", OrderStatus.ACCEPTED, "accept");
                case 5 -> rejectOrder();
                case 6 -> changeStatus("PREPARING", OrderStatus.PREPARING, "start preparing");
                case 7 -> changeStatus("READY", OrderStatus.READY, "mark ready");
                case 8 -> manageMenu();
                default -> io.warn("Unknown option.");
            }
        }
    }

    private void selectRestaurant() throws PlatformException {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        io.success("Working with " + restaurant.describe());
        List<Order> orders = platform.orders().forRestaurant(restaurant.id());
        if (!orders.isEmpty()) {
            io.println();
            io.println("Orders:");
            orders.forEach(order -> io.info(order.id() + " | " + order.status().label() + " | "
                    + order.total() + " | placed " + order.placedAt() + " | " + order.customer().name()));
        }
    }

    private void toggleOpen() {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        io.info(restaurant.name() + " is currently " + (restaurant.isOpen() ? "OPEN" : "CLOSED"));
        if (io.readYesNo("Toggle the status? (y/n): ")) {
            restaurant.setOpen(!restaurant.isOpen());
            io.success(restaurant.name() + " is now " + (restaurant.isOpen() ? "OPEN" : "CLOSED"));
        }
    }

    private void todayReport() {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        List<Order> orders = platform.orders().forRestaurant(restaurant.id()).stream()
                .filter(order -> order.placedDate().equals(today))
                .toList();
        io.heading("TODAY - " + restaurant.name() + " - " + today);
        if (orders.isEmpty()) {
            io.warn("No orders were placed today.");
            return;
        }
        Money revenue = Money.ZERO;
        for (Order order : orders) {
            io.println(String.format("  %-12s %-18s %-22s %10s  %s", order.id(), order.status().label(),
                    order.customer().name(), order.total().format(),
                    order.isPaid() ? "paid" : "unpaid"));
            if (order.status() == OrderStatus.DELIVERED) {
                revenue = revenue.plus(order.total());
            }
        }
        io.println();
        io.success("orders today: " + orders.size() + " | delivered revenue: " + revenue);
    }

    private void changeStatus(String label, OrderStatus target, String action) {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        List<Order> orders = pendingOrders(restaurant, target);
        Order order = menu.pick("ORDERS THAT CAN BE MARKED " + label, orders, candidate ->
                candidate.id() + " | " + candidate.status().label() + " | " + candidate.customer().name()
                        + " | " + candidate.total() + " | placed " + candidate.placedAt());
        if (order == null) {
            return;
        }
        menu.run(action, () -> {
            switch (target) {
                case ACCEPTED -> platform.orderService().restaurantAccept(order.id());
                case PREPARING -> platform.orderService().markPreparing(order.id());
                case READY -> platform.orderService().markReady(order.id());
                default -> throw new LifecycleException(LifecycleException.ILLEGAL_TRANSITION, "unsupported action");
            }
            io.success("Order " + order.id() + " is now " + order.status().label() + ".");
        });
    }

    private void rejectOrder() {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        List<Order> orders = pendingOrders(restaurant, OrderStatus.CANCELLED);
        Order order = menu.pick("PENDING ORDERS", orders, candidate ->
                candidate.id() + " | " + candidate.status().label() + " | " + candidate.customer().name()
                        + " | " + candidate.total());
        if (order == null) {
            return;
        }
        menu.run("reject", () -> {
            String reason = io.readLine("Reason shown to the customer: ");
            platform.orderService().restaurantReject(order.id(), reason);
            io.success("Order " + order.id() + " rejected and cancelled.");
            if (order.isPaid()) {
                io.success("Refunded " + order.paidAmount() + " to the wallet of " + order.customer().name() + ".");
            }
        });
    }

    private List<Order> pendingOrders(Restaurant restaurant, OrderStatus target) {
        OrderStatus required = target == OrderStatus.ACCEPTED ? OrderStatus.PLACED
                : target == OrderStatus.PREPARING ? OrderStatus.ACCEPTED : OrderStatus.PREPARING;
        return platform.orders().forRestaurant(restaurant.id()).stream()
                .filter(order -> order.status() == required)
                .toList();
    }

    private void manageMenu() {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        while (true) {
            io.heading("MENU MANAGEMENT - " + restaurant.name());
            printManagementMenu(restaurant);
            io.println(" 1. Add a menu item");
            io.println(" 2. Remove a menu item");
            io.println(" 3. Toggle item availability");
            io.println(" 4. Adjust the daily stock of an item");
            io.println(" 0. Back");
            int choice = io.readChoice("Choose: ", 5);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> menu.run("add item", () -> addItem(restaurant));
                case 2 -> menu.run("remove item", () -> removeItem(restaurant));
                case 3 -> menu.run("availability", () -> toggleAvailability(restaurant));
                case 4 -> menu.run("stock", () -> adjustStock(restaurant));
                default -> io.warn("Unknown option.");
            }
        }
    }

    private void printManagementMenu(Restaurant restaurant) {
        int number = 0;
        for (MenuItem item : restaurant.menuItems()) {
            number++;
            io.println(String.format("  %2d. %-26s %-9s %-10s %-18s %s",
                    number, item.id(), item.type(), item.unitPrice().format(),
                    item.describeStock(), item.isAvailable() ? "available" : "UNAVAILABLE"));
        }
    }

    private void addItem(Restaurant restaurant) throws PlatformException {
        io.println("Item types:");
        io.println(ItemType.options());
        ItemType type = null;
        while (type == null) {
            String text = io.readLine("Type (STANDARD, COMBO or WEIGHTED): ");
            type = ItemType.parse(text);
            if (type == null) {
                io.warn("Unknown type '" + text + "'. Choose STANDARD, COMBO or WEIGHTED.");
            }
        }
        String id = io.readRequired("Item id (unique on this menu): ");
        String name = io.readRequired("Item name: ");
        String prompt = type == ItemType.WEIGHTED ? "Price per kg in EGP: " : "Price in EGP: ";
        BigDecimal price = io.readPositiveDecimal(prompt);
        String category = io.readRequired("Category (for example Grills, Pizza, Drinks): ");
        int preparationMinutes = io.readIntBetween("Preparation time in minutes (1-240): ", 1, 240);
        int stock = io.readIntBetween("Daily stock in " + (type == ItemType.WEIGHTED ? "kg" : "portions") + " (0-9999): ", 0, 9999);
        boolean available = io.readYesNo("Available today? (y/n): ");
        List<String> componentIds = List.of();
        if (type == ItemType.COMBO) {
            io.println("Current menu items that can go into the combo:");
            restaurant.menuItems().forEach(item -> io.info(item.id() + " | " + item.name() + " | " + item.unitPrice()));
            componentIds = readComponentIds(restaurant);
        }
        MenuItemSpec spec = new MenuItemSpec(type.name(), id, name, price, category,
                preparationMinutes, available, stock, componentIds);
        MenuItem item = MenuItemFactory.create(spec, restaurant);
        restaurant.addMenuItem(item);
        io.success("Added: " + item.name() + " [" + item.type() + "] " + item.describePrice());
    }

    private List<String> readComponentIds(Restaurant restaurant) throws PlatformException {
        List<String> components = new ArrayList<>();
        while (true) {
            String componentId = io.readRequired("Component item id (blank when finished): ");
            if (componentId.isBlank()) {
                break;
            }
            restaurant.requireItem(componentId);
            if (components.contains(componentId)) {
                throw new ValidationException(ValidationException.INVALID_INPUT,
                        "component '" + componentId + "' was already added to this combo");
            }
            components.add(componentId);
        }
        if (components.isEmpty()) {
            throw new ValidationException(ValidationException.EMPTY_ORDER, "a combo needs at least one component");
        }
        return components;
    }

    private void removeItem(Restaurant restaurant) throws PlatformException {
        if (restaurant.menuItems().isEmpty()) {
            io.warn("This menu is already empty.");
            return;
        }
        MenuItem item = menu.pick("REMOVE FROM MENU", restaurant.menuItems(),
                candidate -> candidate.id() + " | " + candidate.name() + " | " + candidate.type());
        if (item == null) {
            return;
        }
        boolean removed = restaurant.removeItem(item.id());
        if (removed) {
            io.success("Removed " + item.name() + " from the menu.");
        } else {
            io.warn("Nothing was removed.");
        }
    }

    private void toggleAvailability(Restaurant restaurant) throws PlatformException {
        if (restaurant.menuItems().isEmpty()) {
            io.warn("This menu is already empty.");
            return;
        }
        MenuItem item = menu.pick("TOGGLE AVAILABILITY", restaurant.menuItems(), candidate ->
                candidate.id() + " | " + candidate.name() + " | "
                        + (candidate.isAvailable() ? "available" : "unavailable"));
        if (item == null) {
            return;
        }
        boolean target = !item.isAvailable();
        restaurant.setItemAvailability(item.id(), target);
        io.success(item.name() + " is now " + (target ? "available" : "unavailable") + ".");
    }

    private void adjustStock(Restaurant restaurant) throws PlatformException {
        if (restaurant.menuItems().isEmpty()) {
            io.warn("This menu is already empty.");
            return;
        }
        MenuItem item = menu.pick("DAILY STOCK", restaurant.menuItems(), candidate ->
                candidate.id() + " | " + candidate.name() + " | " + candidate.describeStock());
        if (item == null) {
            return;
        }
        int stock = io.readIntBetween("New daily stock for " + item.name() + " (0-9999, already sold today: "
                + item.soldToday() + "): ", 0, 9999);
        restaurant.setDailyStock(item.id(), stock);
        io.success(item.name() + " now has " + item.describeStock() + ".");
    }
}
