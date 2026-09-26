package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AdminMenu {

    private final ConsoleIo io;
    private final MenuSupport menu;
    private final Platform platform;

    public AdminMenu(ConsoleIo io, MenuSupport menu) {
        this.io = io;
        this.menu = menu;
        this.platform = menu.platform();
    }

    public void loop() {
        while (true) {
            io.banner("MASR DELIVERY - Admin and Reports");
            io.println(" 1. Add a restaurant");
            io.println(" 2. Remove a restaurant");
            io.println(" 3. Create a promotion");
            io.println(" 4. List promotions");
            io.println(" 5. Run a report");
            io.println(" 6. Platform wide statistics");
            io.println(" 7. Dispatch the next ready order");
            io.println(" 8. Audit log");
            io.println(" 9. Platform configuration");
            io.println("10. Run the self tests");
            io.println(" 0. Back to the main menu");
            int choice = io.readChoice("Choose an option: ", 11);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> menu.run("add restaurant", this::addRestaurant);
                case 2 -> menu.run("remove restaurant", this::removeRestaurant);
                case 3 -> menu.run("create promotion", this::createPromotion);
                case 4 -> listPromotions();
                case 5 -> reports();
                case 6 -> statistics();
                case 7 -> menu.run("dispatch", this::dispatch);
                case 8 -> auditLog();
                case 9 -> configuration();
                case 10 -> selfTests();
                default -> io.warn("Unknown option.");
            }
        }
    }

    private void addRestaurant() throws PlatformException {
        String id = io.readLine("Restaurant id (blank to generate): ");
        if (id.isBlank()) {
            id = platform.nextId("R");
        }
        if (platform.restaurants().exists(id)) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    "restaurant id '" + id + "' is already taken");
        }
        String name = io.readRequired("Display name: ");
        io.println("districts: " + District.options());
        District district = readDistrict();
        Set<Cuisine> cuisines = readCuisines();
        double rating = io.readRating("Average rating 0.0-5.0: ");
        boolean open = io.readYesNo("Open for orders? (y/n): ");
        Restaurant restaurant = new Restaurant(id, name, district, cuisines, rating, open);
        platform.restaurants().add(restaurant);
        io.success("Added " + restaurant.describe());
        if (io.readYesNo("Add menu items now? (y/n): ")) {
            while (io.readYesNo("Add another item? (y/n): ")) {
                menu.run("add item", () -> {
                    io.println("Item types:");
                    io.println(ItemType.options());
                    ItemType type = readItemType();
                    String itemId = io.readRequired("Item id: ");
                    String itemName = io.readRequired("Item name: ");
                    BigDecimal price = io.readPositiveDecimal(
                            type == ItemType.WEIGHTED ? "Price per kg in EGP: " : "Price in EGP: ");
                    String category = io.readRequired("Category: ");
                    int minutes = io.readIntBetween("Preparation time in minutes (1-240): ", 1, 240);
                    int stock = io.readIntBetween("Daily stock (0-9999): ", 0, 9999);
                    List<String> components = type == ItemType.COMBO
                            ? List.of(restaurant.menuItems().stream().map(MenuItem::id).toArray(String[]::new))
                            : List.of();
                    MenuItem item = MenuItemFactory.create(new MenuItemSpec(type.name(), itemId, itemName, price,
                            category, minutes, true, stock, components), restaurant);
                    restaurant.addMenuItem(item);
                    io.success("Added " + item.name());
                });
            }
        }
    }

    private void removeRestaurant() throws PlatformException {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        List<Order> active = platform.orders().forRestaurant(restaurant.id()).stream()
                .filter(Order::isActive)
                .toList();
        if (!active.isEmpty()) {
            throw new ValidationException(ValidationException.INVALID_VALUE,
                    restaurant.name() + " still has " + active.size() + " active order(s) and cannot be removed");
        }
        platform.restaurants().remove(restaurant.id());
        io.success("Removed " + restaurant.name() + ".");
    }

    private void createPromotion() throws PlatformException {
        io.println("promotion kinds: " + String.join(", ", PromotionBuilderRegistry.kinds()));
        String kind = io.readRequired("Kind: ").trim().toUpperCase(Locale.ROOT);
        String code = io.readRequired("Promotion code (case insensitive): ");
        String normalised = Promotion.normaliseCode(code);
        if (platform.promotions().find(normalised).isPresent()) {
            throw new PromotionException(PromotionException.DUPLICATE_CODE,
                    "promotion " + normalised + " already exists");
        }
        Promotion promotion = PromotionBuilderRegistry.build(kind, io, normalised);
        platform.promotions().register(promotion);
        io.success("Registered " + promotion.describe());
    }

    private void listPromotions() {
        List<Promotion> promotions = platform.promotions().all();
        io.heading("PROMOTIONS");
        if (promotions.isEmpty()) {
            io.warn("No promotions are registered.");
            return;
        }
        promotions.forEach(promotion -> io.info(promotion.describe()));
    }

    private void reports() {
        while (true) {
            io.banner("REPORTS");
            io.println(" 1. Total revenue for a date range");
            io.println(" 2. Top five restaurants by revenue for a month");
            io.println(" 3. Average order value per district");
            io.println(" 4. Restaurants rated above 4.5 with at least 20 completed orders");
            io.println(" 5. Order count by status");
            io.println(" 6. Rider deliveries and average delivery time");
            io.println(" 7. Most frequently ordered menu item");
            io.println(" 8. Customer order history and lifetime spend");
            io.println(" 9. Peak ordering hour");
            io.println("10. Customers who have not ordered in the last 30 days");
            io.println(" 0. Back");
            int choice = io.readChoice("Choose a report: ", 11);
            if (choice == 0) {
                return;
            }
            try {
                runReport(choice);
            } catch (PlatformException e) {
                io.error(e.getMessage());
            }
            io.pause();
        }
    }

    private void runReport(int report) throws PlatformException {
        ReportService reports = platform.reports();
        switch (report) {
            case 1 -> {
                io.println("1. TOTAL REVENUE (delivered orders)");
                LocalDate from = io.readDate("From date yyyy-mm-dd: ");
                LocalDate to = io.readDate("To date yyyy-mm-dd: ");
                if (to.isBefore(from)) {
                    throw new ValidationException(ValidationException.INVALID_VALUE, "the end date is before the start date");
                }
                List<Order> delivered = platform.orders().deliveredBetween(from.atStartOfDay(), to.atTime(java.time.LocalTime.MAX));
                io.success("Revenue from " + from + " to " + to + ": " + reports.totalRevenue(from, to)
                        + " across " + delivered.size() + " delivered order(s)");
            }
            case 2 -> {
                io.println("2. TOP FIVE RESTAURANTS BY REVENUE");
                YearMonth month = readYearMonth();
                List<ReportService.RestaurantRevenue> rows = reports.topRestaurantsByRevenue(month);
                if (rows.isEmpty()) {
                    io.warn("No delivered orders in " + month + ".");
                    return;
                }
                rows.forEach(row -> io.info(String.format("%d. %-24s %10s EGP  (%d orders)",
                        rows.indexOf(row) + 1, row.restaurantName(), row.revenue().format(), row.deliveredOrders())));
            }
            case 3 -> {
                io.println("3. AVERAGE ORDER VALUE PER DISTRICT");
                List<ReportService.DistrictAverage> rows = reports.averageOrderValuePerDistrict();
                if (rows.isEmpty()) {
                    io.warn("No delivered orders yet.");
                    return;
                }
                rows.forEach(row -> io.info(row.describe()));
            }
            case 4 -> {
                io.println("4. RESTAURANTS RATED ABOVE 4.5 WITH AT LEAST 20 COMPLETED ORDERS");
                List<ReportService.RestaurantStanding> rows = reports.highlyRatedWithEnoughOrders(4.5d, 20);
                if (rows.isEmpty()) {
                    io.warn("No restaurant is rated above 4.5 with 20 or more completed orders yet.");
                    return;
                }
                rows.forEach(row -> io.info(row.describe()));
            }
            case 5 -> {
                io.println("5. ORDER COUNT BY STATUS");
                Map<OrderStatus, Long> counts = reports.orderCountByStatus();
                counts.forEach((status, count) -> io.info(String.format("%-20s %6d", status.label(), count)));
            }
            case 6 -> {
                io.println("6. RIDER DELIVERIES AND AVERAGE DELIVERY TIME");
                List<ReportService.RiderPerformance> rows = reports.riderPerformance();
                if (rows.isEmpty()) {
                    io.warn("No riders are registered.");
                    return;
                }
                rows.forEach(row -> io.info(row.describe()));
            }
            case 7 -> {
                io.println("7. MOST FREQUENTLY ORDERED MENU ITEM");
                ReportService.MostOrderedItem most = reports.mostFrequentlyOrderedItem();
                if (most.item().isEmpty()) {
                    io.warn("No menu item has ever been ordered, so there is no most ordered item.");
                    return;
                }
                io.success(most.describe());
            }
            case 8 -> {
                io.println("8. CUSTOMER ORDER HISTORY AND LIFETIME SPEND");
                Customer customer = menu.pickCustomer();
                if (customer == null) {
                    return;
                }
                io.println();
                io.print(platform.reports().customerHistory(customer.id()).describe());
            }
            case 9 -> {
                io.println("9. PEAK ORDERING HOUR");
                Optional<ReportService.PeakHour> peak = reports.peakOrderingHour();
                if (peak.isEmpty()) {
                    io.warn("No orders exist yet, so there is no peak hour.");
                    return;
                }
                io.success("Peak ordering hour is " + peak.get().describe());
            }
            case 10 -> {
                io.println("10. CUSTOMERS WHO HAVE NOT ORDERED IN THE LAST 30 DAYS");
                List<ReportService.CustomerInactivity> rows = reports.customersNotOrderedRecently(30);
                if (rows.isEmpty()) {
                    io.success("Every customer has ordered within the last 30 days.");
                    return;
                }
                rows.forEach(row -> io.warn(row.describe()));
            }
            default -> io.warn("Unknown report.");
        }
    }

    private void statistics() {
        io.heading("PLATFORM STATISTICS");
        io.print(platform.reports().platformSnapshot().describe());
        io.println("  cuisines offered: " + platform.restaurants().distinctCuisines().stream()
                .map(Cuisine::label).sorted().toList());
        io.println("  menu items: " + platform.restaurants().stream()
                .mapToInt(Restaurant::menuSize).sum());
        io.println("  live statistics listener events: " + platform.statistics().eventsProcessed());
        platform.statistics().averageDeliveryTime()
                .ifPresent(duration -> io.println("  average delivery time: " + Order.formatDuration(duration)));
    }

    private void dispatch() throws PlatformException {
        List<String> pending = platform.dispatchQueue().pendingLines();
        if (pending.isEmpty()) {
            io.info("The dispatch queue is empty.");
        } else {
            io.println();
            io.println("Orders waiting for a rider (Gold customers first, then longest waiting):");
            pending.forEach(line -> io.info(line));
        }
        OrderService.DispatchResult result = platform.orderService().dispatchNext();
        io.success(result.describe());
    }

    private void auditLog() {
        List<AuditLog.Entry> entries = platform.auditLog().entries();
        io.heading("AUDIT LOG (" + entries.size() + " entries)");
        if (entries.isEmpty()) {
            io.warn("The audit log is empty.");
            return;
        }
        entries.forEach(entry -> io.println("  " + entry));
    }

    private void configuration() {
        io.heading("PLATFORM CONFIGURATION");
        io.print(platform.config().summary());
        io.println("  rider behaviours:");
        RiderBehaviorRegistry.all().forEach((type, behavior) -> io.info(behavior.describe()));
        io.println("  promotion kinds: " + String.join(", ", PromotionBuilderRegistry.kinds()));
        io.println("  menu item kinds: " + java.util.Arrays.toString(ItemType.values()));
    }

    private void selfTests() {
        io.heading("SELF TESTS");
        try {
            new SelfTest().runAll();
        } catch (Exception e) {
            io.error("the self tests could not complete: " + e.getMessage());
        }
    }

    private District readDistrict() {
        while (true) {
            District district = District.parse(io.readRequired("District: "));
            if (district != null) {
                return district;
            }
            io.warn("Unknown district. Valid districts: " + District.options() + ".");
        }
    }

    private Set<Cuisine> readCuisines() {
        io.println("cuisines: " + Cuisine.options());
        Set<Cuisine> cuisines = EnumSet.noneOf(Cuisine.class);
        while (true) {
            String line = io.readOptional("Add a cuisine (blank to finish): ");
            if (line.isBlank()) {
                if (cuisines.isEmpty()) {
                    io.warn("A restaurant needs at least one cuisine.");
                    continue;
                }
                return cuisines;
            }
            Cuisine cuisine = Cuisine.parse(line);
            if (cuisine == null) {
                io.warn("Unknown cuisine '" + line + "'. Valid cuisines: " + Cuisine.options() + ".");
                continue;
            }
            cuisines.add(cuisine);
            io.success(cuisine.label() + " added. Current: " + cuisines.stream().map(Cuisine::label).sorted().toList());
        }
    }

    private ItemType readItemType() {
        while (true) {
            ItemType type = ItemType.parse(io.readLine("Type: "));
            if (type != null) {
                return type;
            }
            io.warn("Choose STANDARD, COMBO or WEIGHTED.");
        }
    }

    private YearMonth readYearMonth() {
        while (true) {
            String text = io.readRequired("Month yyyy-mm (for example " + YearMonth.now() + "): ");
            try {
                return YearMonth.parse(text.trim());
            } catch (java.time.format.DateTimeParseException e) {
                io.warn("'" + text + "' is not a valid month, use yyyy-mm.");
            }
        }
    }
}
