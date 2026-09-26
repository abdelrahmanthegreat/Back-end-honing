package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CustomerMenu {

    private final ConsoleIo io;
    private final MenuSupport menu;
    private final Platform platform;
    private Customer acting;

    public CustomerMenu(ConsoleIo io, MenuSupport menu) {
        this.io = io;
        this.menu = menu;
        this.platform = menu.platform();
    }

    public void loop() {
        while (true) {
            io.banner("MASR DELIVERY - Customer Area");
            io.println(" 1. Select or register a customer");
            io.println(" 2. Browse restaurants (with optional filters)");
            io.println(" 3. Search restaurants (free text)");
            io.println(" 4. View a restaurant menu");
            io.println(" 5. Place an order");
            io.println(" 6. Pay for an order from the wallet");
            io.println(" 7. Track an order");
            io.println(" 8. Cancel an order");
            io.println(" 9. Order history and lifetime spend");
            io.println("10. My recent searches");
            io.println("11. My addresses / top up wallet");
            io.println(" 0. Back to the main menu");
            int choice = io.readChoice("Choose an option: ", 12);
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1 -> manageCustomers();
                case 2 -> browse();
                case 3 -> search();
                case 4 -> viewMenu();
                case 5 -> placeOrder();
                case 6 -> payFromWallet();
                case 7 -> trackOrder();
                case 8 -> cancelOrder();
                case 9 -> orderHistory();
                case 10 -> recentSearches();
                case 11 -> manageProfile();
                default -> io.warn("Unknown option.");
            }
        }
    }

    private void manageCustomers() {
        io.heading("CUSTOMERS");
        io.println(" 1. Use an existing customer");
        io.println(" 2. Register a new customer");
        io.println(" 0. Back");
        int choice = io.readChoice("Choose: ", 3);
        if (choice == 0) {
            return;
        }
        if (choice == 1) {
            Customer customer = menu.pickCustomer();
            if (customer != null) {
                acting = customer;
                io.success("Acting as " + customer.name() + " (" + customer.id() + ").");
            }
            return;
        }
        menu.run("register", () -> {
            Customer customer = registerCustomer();
            acting = customer;
        });
    }

    private Customer registerCustomer() throws PlatformException {
        io.println("Leave a field blank to keep the generated value.");
        String id = io.readLine("Customer id (blank to generate): ");
        if (id.isBlank()) {
            id = platform.nextId("C");
        }
        if (platform.customers().exists(id)) {
            throw new ValidationException(ValidationException.DUPLICATE_ID, "customer id '" + id + "' is already taken");
        }
        String name = io.readRequired("Full name: ");
        String mobile = readMobile("Mobile number (010/011/012/015 + 8 digits): ");
        BigDecimal balance = io.readDecimal("Opening wallet balance in EGP (0 allowed): ");
        if (balance.signum() < 0) {
            throw new ValidationException(ValidationException.INVALID_VALUE, "a wallet balance must not be negative");
        }
        Customer customer = new Customer(id, name, mobile, Money.ofEgp(balance));
        platform.customers().add(customer);
        io.success("Registered " + customer.describe());
        addAddress(customer);
        return customer;
    }

    private void manageProfile() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        while (true) {
            io.heading("CUSTOMER " + customer.id() + " - " + customer.name());
            customer.addressList().forEach(address -> io.info(address.shortLine()));
            io.println("wallet " + customer.balance() + " | tier " + customer.tier().label()
                    + " | completed orders " + customer.completedOrderCount());
            io.println(" 1. Add an address");
            io.println(" 2. Top up the wallet");
            io.println(" 0. Back");
            int choice = io.readChoice("Choose: ", 3);
            if (choice == 0) {
                return;
            }
            menu.run("profile", () -> {
                if (choice == 1) {
                    addAddress(customer);
                } else {
                    customer.topUp(Money.ofEgp(io.readPositiveDecimal("Top up amount in EGP: ")));
                    io.success("New balance " + customer.balance());
                }
            });
        }
    }

    private void addAddress(Customer customer) throws PlatformException {
        while (true) {
            District district = readDistrict("District for the new address (blank to stop): ", true);
            if (district == null) {
                return;
            }
            String detail = io.readRequired("Address detail (street, building, flat): ");
            customer.addAddress(district, detail);
            io.success("Address saved: " + customer.findAddress(detail).map(Address::shortLine).orElse(detail));
            if (!io.readYesNo("Add another address? (y/n): ")) {
                return;
            }
        }
    }

    private void browse() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        RestaurantSearch.SearchCriteria criteria = askCriteria();
        List<Restaurant> results = platform.search().browseOpen(customer, criteria);
        io.println();
        io.println("Filters: " + criteria.describe());
        if (results.isEmpty()) {
            io.warn("No open restaurant matches those filters.");
            return;
        }
        printRestaurants(results);
    }

    private RestaurantSearch.SearchCriteria askCriteria() {
        io.println("Press enter to skip any filter.");
        District district = readDistrict("Filter by district (blank for all) [" + District.options() + "]: ", true);
        Cuisine cuisine = readCuisine("Filter by cuisine (blank for all) [" + Cuisine.options() + "]: ");
        BigDecimal minimumRating = null;
        String ratingText = io.readLine("Minimum rating 0.0-5.0 (blank for all): ");
        if (!ratingText.isBlank()) {
            try {
                BigDecimal candidate = new BigDecimal(ratingText);
                if (candidate.compareTo(BigDecimal.ZERO) < 0 || candidate.compareTo(new BigDecimal("5")) > 0) {
                    io.warn("A rating must be between 0.0 and 5.0 inclusive - filter ignored.");
                } else {
                    minimumRating = candidate;
                }
            } catch (NumberFormatException e) {
                io.warn("'" + ratingText + "' is not a number - filter ignored.");
            }
        }
        Money maximumPrice = null;
        String priceText = io.readLine("Maximum price in EGP (blank for all): ");
        if (!priceText.isBlank()) {
            try {
                Money candidate = Money.ofEgp(new BigDecimal(priceText));
                if (!candidate.isPositive()) {
                    io.warn("A price ceiling must be greater than zero - filter ignored.");
                } else {
                    maximumPrice = candidate;
                }
            } catch (NumberFormatException e) {
                io.warn("'" + priceText + "' is not a valid amount - filter ignored.");
            }
        }
        return new RestaurantSearch.SearchCriteria(district, cuisine, minimumRating, maximumPrice, true, null);
    }

    private void search() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        String freeText = io.readRequired("Search text (name or cuisine): ");
        List<Restaurant> results = platform.search().freeTextSearch(customer, freeText);
        if (results.isEmpty()) {
            io.warn("Nothing found for '" + freeText + "'.");
            return;
        }
        io.success(results.size() + " match(es) for '" + freeText + "':");
        printRestaurants(results);
    }

    private void viewMenu() {
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        if (!restaurant.isOpen()) {
            io.warn(restaurant.name() + " is currently closed.");
        }
        printMenu(restaurant);
    }

    private void printMenu(Restaurant restaurant) {
        io.heading("MENU - " + restaurant.name() + " (" + restaurant.district().label() + ")");
        if (restaurant.menuItems().isEmpty()) {
            io.warn("This restaurant has no menu items yet.");
            return;
        }
        String category = null;
        int number = 0;
        for (MenuItem item : restaurant.menuItems()) {
            if (!item.category().equals(category)) {
                category = item.category();
                io.println();
                io.println("  [" + category + "]");
            }
            number++;
            io.println(String.format("  %2d. %-26s %-10s %-16s %s",
                    number,
                    item.name(),
                    item.describePrice(),
                    item.describeStock(),
                    item.isAvailable() ? "" : "UNAVAILABLE"));
        }
        io.println();
        io.info("Items are listed in the order they were added to the menu.");
    }

    private void placeOrder() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        Restaurant restaurant = menu.pickRestaurant();
        if (restaurant == null) {
            return;
        }
        Map<String, BigDecimal> basket = new LinkedHashMap<>();
        printMenu(restaurant);
        while (true) {
            io.println();
            printBasket(restaurant, basket);
            io.println(" 1. Add an item to the order");
            io.println(" 2. Review the order so far");
            io.println(" 3. Remove an item");
            io.println(" 0. Abandon the order");
            int choice = io.readChoice("Choose: ", 4);
            if (choice == 0) {
                io.warn("Order abandoned, nothing was charged.");
                return;
            }
            if (choice == 1) {
                addToBasket(restaurant, basket);
            } else if (choice == 2) {
                break;
            } else {
                removeFromBasket(restaurant, basket);
            }
        }
        if (basket.isEmpty()) {
            io.error("An order must contain at least one line item. Nothing was charged.");
            return;
        }
        menu.run("place order", () -> finishOrder(customer, restaurant, basket));
    }

    private void printBasket(Restaurant restaurant, Map<String, BigDecimal> basket) {
        if (basket.isEmpty()) {
            io.println("  The order is still empty.");
            return;
        }
        Money running = Money.ZERO;
        for (Map.Entry<String, BigDecimal> entry : basket.entrySet()) {
            MenuItem item = restaurant.findItem(entry.getKey()).orElse(null);
            if (item == null) {
                continue;
            }
            BigDecimal quantity = entry.getValue();
            Money lineTotal = item.unitPrice().times(quantity);
            running = running.plus(lineTotal);
            io.info(entry.getValue().stripTrailingZeros().toPlainString() + " " + item.quantityUnit()
                    + " x " + item.name() + " = " + lineTotal.format() + " EGP");
        }
        io.info("running subtotal: " + running.format() + " EGP (fees and promotions are added at checkout)");
    }

    private void addToBasket(Restaurant restaurant, Map<String, BigDecimal> basket) {
        List<MenuItem> items = restaurant.menuItems();
        MenuItem item = menu.pick("MENU - " + restaurant.name(), items, candidate ->
                candidate.name() + " | " + candidate.describePrice() + " | " + candidate.type()
                        + " | " + candidate.describeStock() + (candidate.isAvailable() ? "" : " | UNAVAILABLE"));
        if (item == null) {
            return;
        }
        BigDecimal quantity = item.type() == ItemType.WEIGHTED
                ? io.readPositiveDecimal("Weight in kg (for example 0.75): ")
                : io.readPositiveDecimal("Quantity (a whole number of " + item.quantityUnit() + "): ");
        try {
            item.validateQuantity(quantity);
        } catch (ValidationException e) {
            io.error(e.getMessage());
            return;
        }
        BigDecimal previous = basket.getOrDefault(item.id(), BigDecimal.ZERO);
        basket.put(item.id(), previous.add(quantity));
        io.success("Order now contains " + (previous.add(quantity)).stripTrailingZeros().toPlainString()
                + " " + item.quantityUnit() + " of " + item.name() + ".");
    }

    private void removeFromBasket(Restaurant restaurant, Map<String, BigDecimal> basket) {
        if (basket.isEmpty()) {
            io.warn("The order is empty.");
            return;
        }
        List<MenuItem> items = restaurant.menuItems().stream().filter(item -> basket.containsKey(item.id())).toList();
        MenuItem item = menu.pick("REMOVE FROM ORDER", items, candidate ->
                candidate.name() + " | " + basket.get(candidate.id()).stripTrailingZeros().toPlainString()
                        + " " + candidate.quantityUnit());
        if (item != null) {
            basket.remove(item.id());
            io.success("Removed " + item.name() + ".");
        }
    }

    private void finishOrder(Customer customer, Restaurant restaurant, Map<String, BigDecimal> basket)
            throws PlatformException {
        List<OrderLine> lines = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : basket.entrySet()) {
            lines.add(OrderLine.of(restaurant.requireItem(entry.getKey()), entry.getValue()));
        }
        Address address = askAddress(customer);
        if (address == null) {
            io.warn("An order needs a delivery address. Nothing was charged.");
            return;
        }
        String promotionCode = io.readLine("Promotion code (blank for none) ["
                + String.join(", ", platform.promotions().codes()) + "]: ");
        String notes = io.readLine("Delivery notes (optional): ");

        io.heading("ORDER PREVIEW");
        lines.forEach(line -> io.info(line.describe()));
        PriceBreakdown preview = platform.pricing().price(customer, restaurant, address, lines,
                promotionCode == null || promotionCode.isBlank() ? null : platform.promotions().require(promotionCode),
                LocalDate.now());
        io.println(preview.describe());
        io.info("Delivery distance: " + platform.geo().distanceKm(restaurant.district(), address.district()) + " km");
        if (!io.readYesNo("Confirm this order? (y/n): ")) {
            io.warn("Order abandoned, nothing was charged.");
            return;
        }
        Order order = platform.orderService().placeOrder(customer, restaurant, address, lines, promotionCode, notes);
        io.success("Order " + order.id() + " placed.");
        io.println(order.describe());
        if (io.readYesNo("Pay " + order.total() + " from the wallet now? (y/n): ")) {
            platform.orderService().payFromWallet(order.id());
            io.success("Paid. New wallet balance " + customer.balance() + ".");
        }
    }

    private Address askAddress(Customer customer) throws PlatformException {
        if (customer.addressList().isEmpty()) {
            io.warn("This customer has no saved address yet.");
            addAddress(customer);
            if (customer.addressList().isEmpty()) {
                return null;
            }
        }
        Address address = menu.pick("DELIVERY ADDRESS", customer.addressList(), Address::shortLine);
        if (address != null) {
            return address;
        }
        if (!io.readYesNo("Add a new address instead? (y/n): ")) {
            return null;
        }
        addAddress(customer);
        return customer.addressList().isEmpty() ? null : customer.addressList().get(customer.addressList().size() - 1);
    }

    private void payFromWallet() {
        menu.run("pay", () -> {
            Customer customer = chooseCustomer();
            if (customer == null) {
                return;
            }
            String reference = menu.askOrderReference();
            Order order = platform.orders().require(reference);
            if (!order.customer().id().equals(customer.id())) {
                throw new ValidationException(ValidationException.INVALID_INPUT,
                        "order " + reference + " belongs to " + order.customer().name());
            }
            io.info("Order total " + order.total() + " | wallet balance " + customer.balance());
            if (order.isPaid()) {
                throw new PaymentException(PaymentException.ALREADY_PAID, "order " + reference + " is already paid");
            }
            platform.orderService().payFromWallet(reference);
            io.success("Paid " + order.total() + ". New balance " + customer.balance() + ".");
        });
    }

    private void trackOrder() {
        menu.run("track", () -> {
            String reference = menu.askOrderReference();
            Order order = platform.orders().require(reference);
            io.println(order.trackingLine());
            io.println();
            order.history().forEach(change -> io.info(change.describe()));
            if (order.status().isActive()) {
                io.println();
                io.info("elapsed since placement: " + Order.formatDuration(order.elapsedSincePlacement()));
                order.deliveryDuration().ifPresent(duration -> io.info("delivery time so far: " + Order.formatDuration(duration)));
            }
        });
    }

    private void cancelOrder() {
        menu.run("cancel", () -> {
            String reference = menu.askOrderReference();
            Order order = platform.orders().require(reference);
            io.info(order.trackingLine());
            if (!order.status().isCancellable()) {
                throw new LifecycleException(LifecycleException.CANNOT_CANCEL,
                        "order " + reference + " is " + order.status().label()
                                + " and can no longer be cancelled");
            }
            String reason = io.readLine("Reason (optional): ");
            Money before = order.customer().balance();
            platform.orderService().cancelOrder(reference, reason);
            io.success("Order " + reference + " cancelled.");
            if (order.isPaid()) {
                io.success("Refunded " + order.paidAmount() + " to the wallet (" + before + " -> "
                        + order.customer().balance() + ").");
            } else {
                io.info("The order was never paid, so nothing was refunded.");
            }
        });
    }

    private void orderHistory() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        ReportService.CustomerHistory history = platform.reports().customerHistory(customer.id());
        io.println();
        io.println(history.describe());
        io.info("tier: " + customer.tier().label() + " | completed orders: " + customer.completedOrderCount()
                + " | lifetime spend: " + history.lifetimeSpent());
    }

    private void recentSearches() {
        Customer customer = chooseCustomer();
        if (customer == null) {
            return;
        }
        List<String> searches = customer.recentSearches();
        if (searches.isEmpty()) {
            io.warn("No searches recorded yet (the last "
                    + platform.config().recentSearchCapacity() + " are kept).");
            return;
        }
        io.println();
        io.println("Last " + searches.size() + " search(es) for " + customer.name() + ", newest first:");
        for (int i = 0; i < searches.size(); i++) {
            io.println("  " + (i + 1) + ". " + searches.get(i));
        }
    }

    private Customer chooseCustomer() {
        Customer customer = acting == null ? menu.pickCustomer() : acting;
        if (customer != null) {
            acting = customer;
        }
        return customer;
    }

    private Cuisine readCuisine(String prompt) {
        while (true) {
            String line = io.readLine(prompt);
            if (line.isBlank()) {
                return null;
            }
            Cuisine cuisine = Cuisine.parse(line);
            if (cuisine != null) {
                return cuisine;
            }
            io.warn("Unknown cuisine '" + line + "'. Valid cuisines: " + Cuisine.options() + ".");
        }
    }

    private String readMobile(String prompt) throws PlatformException {
        while (true) {
            String mobile = io.readRequired(prompt);
            if (Customer.isValidMobile(mobile)) {
                Optional<Customer> existing = platform.customers().findByMobile(mobile);
                if (existing.isPresent()) {
                    throw new ValidationException(ValidationException.DUPLICATE_ID,
                            "mobile " + mobile + " already belongs to " + existing.get().name());
                }
                return mobile;
            }
            io.warn("A mobile number must be exactly 11 digits starting with 010, 011, 012 or 015.");
        }
    }

    private District readDistrict(String prompt, boolean allowBlank) {
        while (true) {
            String line = io.readLine(prompt);
            if (line.isEmpty()) {
                if (allowBlank) {
                    return null;
                }
                io.warn("Please choose a district.");
                continue;
            }
            District district = District.parse(line);
            if (district != null) {
                return district;
            }
            io.warn("Unknown district '" + line + "'. Valid districts: " + District.options() + ".");
        }
    }

    private void printRestaurants(List<Restaurant> restaurants) {
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            io.println(String.format("  %2d. %-24s %-12s %-22s rated %.1f  [%s]",
                    i + 1,
                    restaurant.name(),
                    restaurant.district().label(),
                    restaurant.cuisineLine(),
                    restaurant.rating(),
                    restaurant.isOpen() ? "open" : "CLOSED"));
        }
    }
}
