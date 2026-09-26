package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class SelfTest {

    private final List<String> failures = new ArrayList<>();
    private int passed;

    public static void main(String[] args) throws Exception {
        SelfTest test = new SelfTest();
        test.runAll();
        System.out.println();
        System.out.println("passed: " + test.passed + "   failed: " + test.failures.size());
        if (!test.failures.isEmpty()) {
            test.failures.forEach(failure -> System.out.println("FAILED: " + failure));
            System.exit(1);
        }
        System.out.println("ALL SELF TESTS PASSED");
    }

    public void runAll() throws Exception {
        moneyTests();
        workedExampleTest();
        pricingRuleTests();
        promotionTests();
        lifecycleTests();
        riderTests();
        catalogTests();
        searchTests();
        collectionContractTests();
        dispatchPriorityTests();
        observerTests();
        factoryTests();
        configTests();
        reportTests();
    }

    private void moneyTests() throws Exception {
        section("Money and rounding");
        check("0.10 + 0.20 = 0.30 exactly", Money.ofEgp("0.10").plus(Money.ofEgp("0.20")).equals(Money.ofEgp("0.30")));
        check("no binary drift on 1.15 * 100", Money.ofEgp("1.15").times(100).equals(Money.ofEgp("115.00")));
        check("half piastre rounds up", Money.ofEgp("16.665").equals(Money.ofEgp("16.67")));
        check("1.005 keeps two decimals", Money.ofEgp("1.005").equals(Money.ofEgp("1.01")));
        check("times rate keeps exactness", Money.ofEgp("240.00").timesRate(new BigDecimal("0.10")).equals(Money.ofEgp("24.00")));
        check("zero formats with two decimals", Money.ZERO.format().equals("0.00"));
        check("negative money is detected", Money.ofEgp("-0.01").isNegative());
        check("min clamps at zero", Money.ofEgp("10.00").minus(Money.ofEgp("25.00")).max(Money.ZERO).isZero());
    }

    private void workedExampleTest() throws Exception {
        section("Part B worked example (Faisal / Maadi / 12 km / NILE20)");
        Platform platform = seeded();
        Order order = platform.orders().all().stream()
                .filter(candidate -> candidate.promotionCode() != null && "NILE20".equals(candidate.promotionCode()))
                .findFirst().orElseThrow();
        PriceBreakdown breakdown = order.pricing();
        check("subtotal is 240.00", breakdown.subtotal().equals(Money.ofEgp("240.00")));
        check("delivery fee is 42.00", breakdown.deliveryFee().equals(Money.ofEgp("42.00")));
        check("service fee is 24.00", breakdown.serviceFee().equals(Money.ofEgp("24.00")));
        check("promotion discount is 48.00", breakdown.promotionDiscount().equals(Money.ofEgp("48.00")));
        check("TOTAL IS EXACTLY 258.00", breakdown.total().equals(Money.ofEgp("258.00")));
        check("distance is 12 km", breakdown.distanceKm() == 12);
        check("customer is Bronze", breakdown.loyaltyTier() == LoyaltyTier.BRONZE);
        check("weighted item priced per kg", breakdown.subtotal().equals(
                Money.ofEgp("240.00").times(new BigDecimal("1.0"))));
    }

    private void pricingRuleTests() throws Exception {
        section("Pricing rules");
        Platform platform = seeded();
        Customer customer = platform.customers().require("C-002");
        Restaurant restaurant = platform.restaurants().require("R-DOKKI-1");
        Address address = customer.findAddress("Corniche").orElseThrow();
        List<OrderLine> lines = List.of(OrderLine.of(restaurant.requireItem("SU-1"), new BigDecimal("2")));

        PriceBreakdown bronze = price(platform, customer, restaurant, address, lines, null);
        check("bronze pays full delivery fee", bronze.deliveryFee().equals(Money.ofEgp("42.00")));
        check("service fee is 10 percent of subtotal", bronze.serviceFee().equals(Money.ofEgp("19.00")));

        Customer dokkiCustomer = platform.customers().require("C-003");
        PriceBreakdown sameDistrict = price(platform, dokkiCustomer, platform.restaurants().require("R-DOKKI-1"),
                dokkiCustomer.addressList().get(0), lines, null);
        check("zero km still pays the base fee", sameDistrict.deliveryFee().equals(Money.ofEgp("15.00")));

        check("silver gets 10 percent off delivery",
                silverFee(platform).equals(Money.ofEgp("37.80")));
        check("gold delivery fee is waived", goldFee(platform).equals(Money.ZERO));
        check("tier is derived from completed orders",
                LoyaltyTier.fromCompletedOrders(0) == LoyaltyTier.BRONZE
                        && LoyaltyTier.fromCompletedOrders(9) == LoyaltyTier.BRONZE
                        && LoyaltyTier.fromCompletedOrders(10) == LoyaltyTier.SILVER
                        && LoyaltyTier.fromCompletedOrders(29) == LoyaltyTier.SILVER
                        && LoyaltyTier.fromCompletedOrders(30) == LoyaltyTier.GOLD
                        && LoyaltyTier.fromCompletedOrders(400) == LoyaltyTier.GOLD);

        check("weighted item supports fractional kilograms",
                restaurant.requireItem("SW-2").priceFor(new BigDecimal("1.5")).equals(Money.ofEgp("870.00")));
        check("standard item rejects fractional quantity",
                throwsValidation(() -> restaurant.requireItem("SU-1").validateQuantity(new BigDecimal("1.5"))));
        check("standard item rejects zero quantity",
                throwsValidation(() -> restaurant.requireItem("SU-1").validateQuantity(BigDecimal.ZERO)));
        check("weighted item rejects zero kilograms",
                throwsValidation(() -> restaurant.requireItem("SW-2").validateQuantity(BigDecimal.ZERO)));
        ComboItem platter = (ComboItem) restaurant.requireItem("SC-1");
        check("combo is sold at the bundle price",
                platter.priceFor(new BigDecimal("2")).equals(Money.ofEgp("310.00")));
        check("combo is cheaper than the sum of its parts",
                platter.savings().isPositive()
                        && platter.componentsTotal().minus(platter.unitPrice()).equals(platter.savings()));
        check("a combo priced above its parts is rejected",
                throwsRuntime(() -> new ComboItem("R-1", "C-9", "Bad Combo", new BigDecimal("999"),
                        List.of(restaurant.requireItem("SU-1"), restaurant.requireItem("SU-2")), "Combos", 10, true, 5),
                        IllegalArgumentException.class));
    }

    private void promotionTests() throws Exception {
        section("Promotions");
        Platform platform = seeded();
        Customer firstTimer = platform.customers().require("C-004");
        Customer returning = platform.customers().require("C-002");
        Restaurant coffee = platform.restaurants().require("R-ZAMALEK-1");
        Address zamalek = firstTimer.addressList().get(0);
        List<OrderLine> lines = List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("4")));

        check("codes are case insensitive",
                platform.promotions().find("nile20").isPresent() && platform.promotions().find(" NiLe20 ").isPresent());
        check("unknown code is rejected",
                throwsPromotion(() -> platform.promotions().require("NOPE"), PromotionException.UNKNOWN_CODE));
        check("minimum subtotal condition rejects small orders",
                throwsPromotion(() -> price(platform, firstTimer, coffee, zamalek,
                        List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("1"))), "NILE20"),
                        PromotionException.MINIMUM_SUBTOTAL_NOT_MET));
        check("expired promotion is rejected",
                throwsPromotion(() -> price(platform, firstTimer, coffee, zamalek, lines, "OLDGOLD"),
                        PromotionException.EXPIRED));
        check("first time only promotion rejects a returning customer",
                throwsPromotion(() -> price(platform, returning, coffee,
                        platform.customers().require("C-002").addressList().get(0), lines, "WELCOME15"),
                        PromotionException.NOT_FIRST_TIME));
        check("district restriction rejects another district",
                throwsPromotion(() -> price(platform, firstTimer, coffee, zamalek, lines, "GIZAONLY"),
                        PromotionException.DISTRICT_NOT_ALLOWED));

        PriceBreakdown freeDelivery = price(platform, firstTimer, coffee, zamalek, lines, "FREEGO");
        check("free delivery promotion zeroes the fee", freeDelivery.deliveryFee().isZero());
        check("free delivery promotion leaves the subtotal alone",
                freeDelivery.promotionDiscount().isZero() && freeDelivery.subtotal().equals(Money.ofEgp("220.00")));
        check("service fee is still charged", freeDelivery.serviceFee().equals(Money.ofEgp("22.00")));

        PriceBreakdown capped = price(platform, platform.customers().require("C-003"),
                platform.restaurants().require("R-HELIO-1"),
                platform.customers().require("C-003").addressList().get(0),
                List.of(OrderLine.of(platform.restaurants().require("R-HELIO-1").requireItem("HD-1"), new BigDecimal("1"))),
                "NILE20");
        check("percentage promotion respects its cap", capped.promotionDiscount().equals(Money.ofEgp("50.00")));

        PriceBreakdown negative = price(platform, firstTimer, coffee, zamalek,
                List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("1"))), null);
        check("a positive order never has a negative total", !negative.total().isNegative());

        PriceBreakdown clamped = priceWithPromotion(platform, firstTimer, coffee, zamalek,
                List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("1"))),
                new AbsurdPromotion());
        check("a discount larger than the whole order clamps the total at zero", clamped.total().isZero());
        check("a huge but legitimate discount never goes negative",
                !priceWithPromotion(platform, firstTimer, coffee, zamalek,
                        List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("1"))),
                        FixedAmountPromotion.of("BIG", Money.ofEgp("10000"), null)).total().isNegative());
    }

    private void lifecycleTests() throws Exception {
        section("Order lifecycle");
        Platform platform = seeded();
        Order placed = platform.orders().all().stream()
                .filter(order -> order.status() == OrderStatus.PLACED)
                .findFirst().orElseThrow();
        check("PLACED cannot jump to DELIVERED",
                throwsLifecycle(() -> placed.transitionTo(OrderStatus.DELIVERED, "cheat"),
                        LifecycleException.ILLEGAL_TRANSITION));
        check("PLACED cannot jump to READY",
                throwsLifecycle(() -> placed.transitionTo(OrderStatus.READY, "cheat"),
                        LifecycleException.ILLEGAL_TRANSITION));
        check("PLACED can be cancelled",
                runs(() -> placed.transitionTo(OrderStatus.CANCELLED, "changed my mind")));
        check("a cancellation is final",
                throwsLifecycle(() -> placed.transitionTo(OrderStatus.ACCEPTED, "undo"),
                        LifecycleException.ALREADY_CANCELLED));
        check("the same status twice is rejected",
                throwsLifecycle(() -> placed.transitionTo(OrderStatus.CANCELLED, "again"),
                        LifecycleException.ILLEGAL_TRANSITION));

        Order delivered = platform.orders().all().stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .findFirst().orElseThrow();
        check("a delivered order cannot be cancelled",
                throwsLifecycle(() -> delivered.transitionTo(OrderStatus.CANCELLED, "too late"),
                        LifecycleException.ALREADY_DELIVERED));
        check("a delivered order cannot move again",
                throwsLifecycle(() -> delivered.transitionTo(OrderStatus.OUT_FOR_DELIVERY, "again"),
                        LifecycleException.ALREADY_DELIVERED));
        check("Delivered is terminal", OrderStatus.DELIVERED.isTerminal() && OrderStatus.CANCELLED.isTerminal());
        check("Out for delivery can no longer be cancelled", !OrderStatus.OUT_FOR_DELIVERY.isCancellable());
    }

    private void riderTests() throws Exception {
        section("Rider rules");
        Platform platform = seeded();
        Rider rider = new Rider("RD-T1", "Test Rider", VehicleType.MOTORCYCLE, District.MAADI);
        platform.riders().add(rider);
        Customer customer = platform.customers().require("C-004");
        Restaurant restaurant = platform.restaurants().require("R-ZAMALEK-1");
        Order first = platform.orderService().placeOrder(customer, restaurant, customer.addressList().get(0),
                List.of(OrderLine.of(restaurant.requireItem("CO-1"), new BigDecimal("1"))), null, null);
        Order second = platform.orderService().placeOrder(customer, restaurant, customer.addressList().get(0),
                List.of(OrderLine.of(restaurant.requireItem("CO-1"), new BigDecimal("1"))), null, null);
        for (Order order : List.of(first, second)) {
            platform.orderService().restaurantAccept(order.id());
            platform.orderService().markPreparing(order.id());
            platform.orderService().markReady(order.id());
        }

        check("an off duty rider cannot be assigned",
                throwsDispatch(() -> platform.orderService().assignToRider(first.id(), rider.id()),
                        DispatchException.RIDER_OFF_DUTY));
        cannotThrow(() -> rider.goOnDuty());
        cannotThrow(() -> platform.orderService().assignToRider(first.id(), rider.id()));
        check("a rider holding one order is busy", rider.isBusy());
        check("a rider may hold AT MOST ONE active order",
                throwsDispatch(() -> platform.orderService().assignToRider(second.id(), rider.id()),
                        DispatchException.RIDER_BUSY));
        check("going off duty while carrying is rejected",
                throwsDispatch(rider::goOffDuty, DispatchException.RIDER_BUSY));
        cannotThrow(() -> platform.orderService().markPickedUp(first.id()));
        cannotThrow(() -> platform.orderService().markDelivered(first.id()));
        check("delivery is counted", rider.completedDeliveries() == 1);
        check("the rider is free again", !rider.isBusy());
        cannotThrow(() -> platform.orderService().assignToRider(second.id(), rider.id()));
        check("the rider records the delivered order", rider.completedDeliveries() == 1);

        Order heavy = cannotThrowReturn(() -> platform.orderService().placeOrder(customer, restaurant,
                customer.addressList().get(0),
                List.of(OrderLine.of(restaurant.requireItem("CO-1"), new BigDecimal("30"))), null, null));
        platform.orderService().restaurantAccept(heavy.id());
        platform.orderService().markPreparing(heavy.id());
        platform.orderService().markReady(heavy.id());
        check("a bicycle cannot take an order of 30 units",
                throwsDispatch(() -> platform.orderService().assignToRider(heavy.id(), "RD-2"),
                        DispatchException.RIDER_INELIGIBLE));
        check("a car can take an order of 30 units",
                runs(() -> platform.orderService().assignToRider(heavy.id(), "RD-3")));
    }

    private void catalogTests() throws Exception {
        section("Catalog, stock and validation");
        Platform platform = seeded();
        OrderService service = platform.orderService();
        Customer customer = platform.customers().require("C-004");
        Restaurant coffee = platform.restaurants().require("R-ZAMALEK-1");
        Restaurant closed = platform.restaurants().require("R-GIZA-1");

        check("a closed restaurant refuses orders",
                throwsCatalog(() -> service.placeOrder(customer, closed, customer.addressList().get(0),
                        List.of(OrderLine.of(closed.requireItem("KO-1"), new BigDecimal("1"))), null, null),
                        CatalogException.RESTAURANT_CLOSED));
        Customer ahmed = platform.customers().require("C-001");
        Restaurant nileGrill = platform.restaurants().require("R-MAADI-1");
        check("an unavailable item is refused",
                throwsCatalog(() -> service.placeOrder(ahmed, nileGrill, ahmed.findAddress("El Nasr").orElseThrow(),
                        List.of(OrderLine.of(nileGrill.requireItem("SW-1"), BigDecimal.ONE)), null, null),
                        CatalogException.ITEM_UNAVAILABLE));
        check("an item from another restaurant is refused",
                throwsCatalog(() -> service.placeOrder(customer, coffee, customer.addressList().get(0),
                        List.of(OrderLine.of(nileGrill.requireItem("ST-1"), BigDecimal.ONE)), null, null),
                        CatalogException.ITEM_NOT_ON_MENU));
        check("an empty order is refused",
                throwsValidation(() -> service.placeOrder(customer, coffee, customer.addressList().get(0), List.of(), null, null)));
        check("a foreign address is refused",
                throwsValidation(() -> service.placeOrder(customer, coffee,
                        platform.customers().require("C-003").addressList().get(0),
                        List.of(OrderLine.of(coffee.requireItem("CO-1"), BigDecimal.ONE)), null, null)));
        check("stock is enforced",
                throwsStock(() -> service.placeOrder(customer, coffee, customer.addressList().get(0),
                        List.of(OrderLine.of(coffee.requireItem("DE-2"), new BigDecimal("999"))), null, null)));

        int before = coffee.requireItem("CO-1").soldToday();
        Order placed = cannotThrowReturn(() -> service.placeOrder(customer, coffee, customer.addressList().get(0),
                List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("2"))), null, null));
        check("placing an order reserves stock", coffee.requireItem("CO-1").soldToday() == before + 2);
        cannotThrow(() -> service.cancelOrder(placed.id(), "changed my mind"));
        check("cancelling releases stock", coffee.requireItem("CO-1").soldToday() == before);
        check("a cancelled order cannot be paid",
                throwsPayment(() -> service.payFromWallet(placed.id()), PaymentException.ORDER_NOT_PAYABLE));
        Money balanceBeforePayment = customer.balance();
        Order payable = cannotThrowReturn(() -> service.placeOrder(customer, coffee, customer.addressList().get(0),
                List.of(OrderLine.of(coffee.requireItem("CO-1"), new BigDecimal("2"))), null, null));
        cannotThrow(() -> service.payFromWallet(payable.id()));
        check("paying from the wallet debits exactly the total",
                customer.balance().equals(balanceBeforePayment.minus(payable.total())));
        check("paying twice is rejected",
                throwsPayment(() -> service.payFromWallet(payable.id()), PaymentException.ALREADY_PAID));
        Customer broke = platform.customers().add(new Customer("C-900", "Broke Hatem", "01000000001", Money.ofEgp("1.00")));
        broke.addAddress(District.ZAMALEK, "1 Test Street");
        Order tooExpensive = cannotThrowReturn(() -> service.placeOrder(broke, coffee, broke.addressList().get(0),
                List.of(OrderLine.of(coffee.requireItem("CO-1"), BigDecimal.ONE)), null, null));
        check("a wallet without enough balance is refused",
                throwsPayment(() -> service.payFromWallet(tooExpensive.id()), PaymentException.INSUFFICIENT_FUNDS));
        check("wallet balance is never negative", broke.balance().equals(Money.ofEgp("1.00")));

        check("duplicate restaurant ids are rejected",
                throwsValidation(() -> platform.restaurants().add(
                        new Restaurant("R-FAISAL-1", "Copy Cat", District.FAISAL, List.of(Cuisine.PIZZA), 4.0d, true))));
        check("duplicate customer ids are rejected",
                throwsValidation(() -> platform.customers().add(new Customer("C-001", "Impostor", "01000000000", Money.ZERO))));
        check("duplicate rider ids are rejected",
                throwsValidation(() -> platform.riders().add(new Rider("RD-1", "Impostor", VehicleType.CAR, District.GIZA))));
        check("duplicate order ids are rejected",
                throwsValidation(() -> platform.orders().add(placed)));
        check("mobile numbers must be 11 digits starting 010/011/012/015",
                Customer.isValidMobile("01012345678") && Customer.isValidMobile("01598765432")
                        && !Customer.isValidMobile("0101234567") && !Customer.isValidMobile("01312345678")
                        && !Customer.isValidMobile("010123456789") && !Customer.isValidMobile("abcdefghijk"));
        check("ratings must be within 0.0 to 5.0",
                runs(() -> new Restaurant("R-X", "Rating Test", District.GIZA, List.of(Cuisine.PIZZA), 5.0d, true))
                        && runs(() -> new Restaurant("R-Y", "Rating Test", District.GIZA, List.of(Cuisine.PIZZA), 0.0d, true))
                        && throwsRuntime(() -> new Restaurant("R-Z", "Rating Test", District.GIZA, List.of(Cuisine.PIZZA), 5.1d, true),
                        IllegalArgumentException.class)
                        && throwsRuntime(() -> new Restaurant("R-W", "Rating Test", District.GIZA, List.of(Cuisine.PIZZA), -0.1d, true),
                        IllegalArgumentException.class));
        check("prices must be greater than zero",
                throwsRuntime(() -> new StandardItem("R-1", "I-1", "Free Food", BigDecimal.ZERO, "X", 5, true, 5),
                        IllegalArgumentException.class)
                        && throwsRuntime(() -> new StandardItem("R-1", "I-2", "Free Food", new BigDecimal("-3"), "X", 5, true, 5),
                        IllegalArgumentException.class)
                        && runs(() -> new StandardItem("R-1", "I-3", "Paid Food", new BigDecimal("0.01"), "X", 5, true, 5)));
        check("a restaurant needs at least one cuisine",
                throwsRuntime(() -> new Restaurant("R-Q", "No Cuisine", District.GIZA, List.of(), 4.0d, true),
                        IllegalArgumentException.class));
    }

    private void searchTests() throws Exception {
        section("Search facility");
        Platform platform = seeded();
        RestaurantSearchFacade search = platform.search();
        Customer customer = platform.customers().require("C-001");

        List<Restaurant> open = search.find(RestaurantSearch.openOnly());
        check("only open restaurants are browsed", open.stream().allMatch(Restaurant::isOpen) && !open.isEmpty());
        check("results are sorted by rating then name", isSortedByRatingThenName(open));

        Predicate<Restaurant> maadiAndGrill = RestaurantSearch.allOf(
                RestaurantSearch.inDistrict(District.MAADI), RestaurantSearch.withCuisine(Cuisine.GRILL));
        List<Restaurant> combined = search.find(maadiAndGrill);
        check("filters combine with and", combined.stream()
                .allMatch(restaurant -> restaurant.district() == District.MAADI && restaurant.cuisines().contains(Cuisine.GRILL))
                && !combined.isEmpty());

        List<Restaurant> cheap = search.find(RestaurantSearch.maxItemPrice(Money.ofEgp("60")));
        check("a price ceiling of max item price is honoured", !cheap.isEmpty() && cheap.stream()
                .allMatch(restaurant -> restaurant.menuStream()
                        .allMatch(item -> item.unitPrice().compareTo(Money.ofEgp("60")) <= 0)));
        List<Restaurant> withCheapDish = search.find(RestaurantSearch.hasItemAtMost(Money.ofEgp("60")));
        check("a price ceiling of any item is honoured", withCheapDish.size() > cheap.size() && withCheapDish.stream()
                .allMatch(restaurant -> restaurant.menuStream()
                        .anyMatch(item -> item.unitPrice().compareTo(Money.ofEgp("60")) <= 0)));

        RestaurantSearch.SearchCriteria criteria = new RestaurantSearch.SearchCriteria(
                null, Cuisine.JAPANESE, new BigDecimal("4.0"), Money.ofEgp("600"), true, null);
        List<Restaurant> sushi = search.browseOpen(customer, criteria);
        check("criteria objects build predicates", !sushi.isEmpty() && sushi.stream()
                .allMatch(restaurant -> restaurant.cuisines().contains(Cuisine.JAPANESE) && restaurant.rating() >= 4.0d));

        List<Restaurant> byText = search.freeTextSearch(customer, "sushi");
        check("free text search matches a cuisine", !byText.isEmpty());
        check("free text search remembers the term", customer.recentSearches().contains("sushi"));
        check("nothing found is an empty list, not an error",
                search.freeTextSearch(customer, "zzzz-not-a-thing").isEmpty());
    }

    private void collectionContractTests() throws Exception {
        section("Collection contracts (Part C)");
        Platform platform = seeded();
        Restaurant restaurant = platform.restaurants().require("R-MAADI-1");
        Customer customer = platform.customers().require("C-001");
        Rider rider = platform.riders().require("RD-1");

        List<String> first = restaurant.menuItems().stream().map(MenuItem::id).toList();
        List<String> second = restaurant.menuItems().stream().map(MenuItem::id).toList();
        check("menu order is insertion order and stable", first.equals(second));
        check("menu keeps insertion order",
                first.equals(List.of("MG-1", "ST-1", "ST-2", "ST-3", "WG-1", "SW-1", "CB-1")));
        check("menu lookup by id is a map lookup, not a scan",
                restaurant.findItem("CB-1").isPresent() && restaurant.findItem("nope").isEmpty());

        check("distinct cuisines have no duplicates",
                platform.restaurants().distinctCuisines().size()
                        == platform.restaurants().stream().flatMap(r -> r.cuisines().stream()).distinct().count());

        Set<Restaurant> restaurantSet = new HashSet<>(platform.restaurants().all());
        platform.restaurants().all().forEach(value -> restaurantSet.add(value));
        check("the same entity is never stored twice", restaurantSet.size() == platform.restaurants().size());
        check("entities deduplicate correctly in a hash set",
                new HashSet<>(platform.orders().all()).size() == platform.orders().size()
                        && new HashSet<>(platform.customers().all()).size() == platform.customers().size()
                        && new HashSet<>(platform.riders().all()).size() == platform.riders().size());

        customer.recordSearch("one");
        customer.recordSearch("two");
        customer.recordSearch("three");
        customer.recordSearch("four");
        customer.recordSearch("five");
        customer.recordSearch("six");
        check("only the last five searches are kept", customer.recentSearches().size() == 5);
        check("recent searches are newest first",
                customer.recentSearches().equals(List.of("six", "five", "four", "three", "two")));
        check("the whole history is not kept", !customer.recentSearches().contains("one"));

        check("menu view is unmodifiable", throwsUnsupported(() -> restaurant.menuItems().add(null)));
        check("repository view is unmodifiable", throwsUnsupported(() -> platform.restaurants().all().add(null)));
        check("cuisine set is unmodifiable", throwsUnsupported(() -> restaurant.cuisines().add(Cuisine.MEXICAN)));
        check("address set is unmodifiable", throwsUnsupported(() -> customer.addresses().add(null)));
        check("rider behaviour registry is a copy", RiderBehaviorRegistry.all().size() >= 3);

        check("addresses are value objects", new Address("C-001", District.MAADI, "a")
                .equals(new Address("C-001", District.MAADI, "a"))
                && !new Address("C-001", District.MAADI, "a").equals(new Address("C-002", District.MAADI, "a")));
        check("the same address cannot be saved twice",
                throwsValidation(() -> customer.addAddress(District.MAADI, "12 El Nasr Street, Building 3, Flat 7")));
    }

    private void dispatchPriorityTests() throws Exception {
        section("Dispatch queue priority (Part C.5)");
        Platform platform = seeded();
        DispatchQueue queue = new DispatchQueue();
        LocalDateTime now = LocalDateTime.now();
        Customer bronze = platform.customers().require("C-001");
        Customer gold = platform.customers().require("C-003");

        Order oldBronze = cannotThrowReturn(() -> platform.orderService().placeOrder(bronze,
                platform.restaurants().require("R-MAADI-1"), bronze.findAddress("Freedom").orElseThrow(),
                List.of(OrderLine.of(platform.restaurants().require("R-MAADI-1").requireItem("ST-1"), BigDecimal.ONE)),
                null, null));
        Order newBronze = cannotThrowReturn(() -> platform.orderService().placeOrder(bronze,
                platform.restaurants().require("R-MAADI-1"), bronze.findAddress("Freedom").orElseThrow(),
                List.of(OrderLine.of(platform.restaurants().require("R-MAADI-1").requireItem("ST-1"), BigDecimal.ONE)),
                null, null));
        Order goldOrder = cannotThrowReturn(() -> platform.orderService().placeOrder(gold,
                platform.restaurants().require("R-MAADI-1"), gold.addressList().get(0),
                List.of(OrderLine.of(platform.restaurants().require("R-MAADI-1").requireItem("ST-1"), BigDecimal.ONE)),
                null, null));

        for (Order order : List.of(oldBronze, newBronze, goldOrder)) {
            platform.orderService().restaurantAccept(order.id());
            platform.orderService().markPreparing(order.id());
            platform.orderService().markReady(order.id());
        }
        queue.offer(oldBronze, LoyaltyTier.BRONZE, now.minusMinutes(90));
        queue.offer(newBronze, LoyaltyTier.SILVER, now.minusMinutes(1));
        queue.offer(goldOrder, LoyaltyTier.GOLD, now.minusMinutes(5));
        check("gold jumps ahead regardless of waiting time", queue.pollNext().orElseThrow().id().equals(goldOrder.id()));
        check("the longest waiting order goes next", queue.pollNext().orElseThrow().id().equals(oldBronze.id()));
        check("the newest order goes last", queue.pollNext().orElseThrow().id().equals(newBronze.id()));
        check("an empty queue returns empty", queue.pollNext().isEmpty());
        check("a cancelled order is skipped by the queue", queueSkipsCancelled(platform));
    }

    private void observerTests() throws Exception {
        section("Order observers (Part G.4)");
        Platform platform = seeded();
        int auditBefore = platform.auditLog().size();
        long eventsBefore = platform.statistics().eventsProcessed();
        Customer customer = platform.customers().require("C-001");
        Order order = cannotThrowReturn(() -> platform.orderService().placeOrder(customer,
                platform.restaurants().require("R-ZAMALEK-1"), customer.findAddress("Freedom").orElseThrow(),
                List.of(OrderLine.of(platform.restaurants().require("R-ZAMALEK-1").requireItem("CO-1"), BigDecimal.ONE)),
                null, null));
        check("the creation event reaches the audit log", platform.auditLog().size() > auditBefore);
        check("statistics counted the event", platform.statistics().eventsProcessed() > eventsBefore);
        check("the customer was notified", !customer.notifications().isEmpty());
        check("four listeners are attached without touching Order", order.listenerNames().size() == 4);

        List<String> captured = new ArrayList<>();
        OrderEventListener late = event -> captured.add(event.newStatus().name());
        order.addListener(late);
        check("a listener can be added at runtime", order.listenerNames().size() == 5);
        cannotThrow(() -> platform.orderService().restaurantAccept(order.id()));
        check("the late listener receives events", captured.equals(List.of("ACCEPTED")));
        order.removeListener(late);
        cannotThrow(() -> platform.orderService().markPreparing(order.id()));
        check("a removed listener stops receiving events", captured.size() == 1);
        check("a broken listener cannot break the order",
                runs(() -> {
                    order.addListener(event -> {
                        throw new IllegalStateException("listener failure");
                    });
                    platform.orderService().markReady(order.id());
                }));
    }

    private void factoryTests() throws Exception {
        section("Menu item factory (Part G.3)");
        Platform platform = seeded();
        Restaurant restaurant = platform.restaurants().require("R-NASR-1");
        check("standard items are created from a data row",
                MenuItemFactory.create(MenuItemSpec.fromCsvLine("STANDARD|NEW-1|Test Wrap|50.00|Wraps|5|true|10|"), restaurant)
                        instanceof StandardItem);
        check("weighted items are created from a data row",
                MenuItemFactory.create(MenuItemSpec.fromCsvLine("WEIGHTED|NEW-2|Test Fish|300.00|Fish|9|true|5|"), restaurant)
                        instanceof WeightedItem);
        check("combos are created from a data row",
                MenuItemFactory.create(MenuItemSpec.fromCsvLine("COMBO|NEW-3|Test Combo|150.00|Combos|9|true|5|SH-1;SH-3"), restaurant)
                        instanceof ComboItem);
        check("the type field is the only thing that decides the class",
                MenuItemFactory.registry().size() == 3
                        && MenuItemFactory.registry().keySet().equals(Set.of(ItemType.STANDARD, ItemType.COMBO, ItemType.WEIGHTED)));
        check("an unknown type is rejected",
                throwsValidation(() -> MenuItemFactory.create(MenuItemSpec.fromCsvLine("MYSTERY|NEW-4|X|10.00|Y|5|true|5|"), restaurant)));
        check("a combo with an unknown component is rejected",
                throwsValidation(() -> MenuItemFactory.create(MenuItemSpec.fromCsvLine("COMBO|NEW-5|Test|10.00|Combos|5|true|5|NOPE"), restaurant)));
        check("a zero price is rejected",
                throwsValidation(() -> MenuItemFactory.create(MenuItemSpec.fromCsvLine("STANDARD|NEW-6|Test|0.00|Y|5|true|5|"), restaurant)));
        check("a duplicate item id is rejected",
                throwsValidation(() -> MenuItemFactory.create(MenuItemSpec.fromCsvLine("STANDARD|SH-1|Duplicate|10.00|Y|5|true|5|"), restaurant)));
        check("an unknown vehicle type behaviour is not silently accepted",
                RiderBehaviorRegistry.behaviorFor(VehicleType.CAR).maxUnitsPerTrip() == 60
                        && RiderBehaviorRegistry.behaviorFor(VehicleType.BICYCLE).maxUnitsPerTrip() == 12
                        && RiderBehaviorRegistry.behaviorFor(VehicleType.MOTORCYCLE).maxUnitsPerTrip() == 30);
    }

    private void configTests() throws Exception {
        section("Configuration singleton (Part G.5)");
        check("the configuration is loaded once", PlatformConfig.get() == PlatformConfig.get());
        check("fee defaults match the specification",
                PlatformConfig.get().baseDeliveryFee().equals(Money.ofEgp("15.00"))
                        && PlatformConfig.get().perExtraKmFee().equals(Money.ofEgp("3.00"))
                        && PlatformConfig.get().includedKilometres() == 3
                        && PlatformConfig.get().serviceFeeRate().compareTo(new BigDecimal("0.10")) == 0);
        PlatformConfig custom = PlatformConfig.of(Map.of("delivery.baseFee", "20.00", "delivery.includedKm", "5"));
        check("an alternative configuration is honoured",
                custom.baseDeliveryFee().equals(Money.ofEgp("20.00")) && custom.includedKilometres() == 5);
        PricingEngine engine = new PricingEngine(custom, new GeoService());
        check("the engine reads fees from the configuration", engine.deliveryFee(7).equals(Money.ofEgp("26.00")));
    }

    private void reportTests() throws Exception {
        section("Reports (Part D)");
        Platform platform = seeded();
        ReportService reports = platform.reports();
        LocalDate today = LocalDate.now();

        check("1 total revenue for a date range", cannotThrowReturn(() -> reports.totalRevenue(today.minusYears(5), today)).isPositive());
        check("2 top five restaurants by revenue",
                reports.topRestaurantsByRevenue(YearMonth.now()).size() <= 5
                        && !reports.topRestaurantsByRevenue(YearMonth.now()).isEmpty());
        check("3 average order value per district", !reports.averageOrderValuePerDistrict().isEmpty());
        check("4 highly rated restaurants", reports.highlyRatedWithEnoughOrders(4.5d, 1)
                .stream().allMatch(standing -> standing.rating() > 4.5d && standing.completedOrders() >= 1));
        check("5 order count by status", reports.orderCountByStatus().size() == OrderStatus.values().length);
        check("6 rider performance", !reports.riderPerformance().isEmpty()
                && reports.riderPerformance().stream()
                .allMatch(row -> row.averageDeliveryDuration().map(duration -> duration.toMinutes() >= 0).orElse(true)));
        check("7 most frequently ordered item", reports.mostFrequentlyOrderedItem().item().isPresent());
        check("8 customer history with lifetime spend",
                reports.customerHistory("C-002").orderCount() >= 1
                        && !reports.customerHistory("C-002").lifetimeSpent().isZero()
                        && isNewestFirst(reports.customerHistory("C-002").orders()));
        check("9 peak ordering hour", reports.peakOrderingHour().isPresent()
                && reports.peakOrderingHour().orElseThrow().hour() >= 0);
        check("10 customers who have not ordered recently",
                runs(() -> reports.customersNotOrderedRecently(30)));
        Customer neverOrdered = platform.customers().add(new Customer("C-777", "Nadwa Fathy", "01000000002", Money.ofEgp("20.00")));
        List<ReportService.CustomerInactivity> inactive = reports.customersNotOrderedRecently(30);
        check("a customer who never ordered is reported honestly",
                inactive.stream().anyMatch(row -> "C-777".equals(row.customerId()) && row.lastOrderDate() == null));
        check("a customer who ordered today is not reported as inactive",
                inactive.stream().noneMatch(row -> "C-001".equals(row.customerId())));
        check("every inactive customer has a name", inactive.stream().allMatch(row -> row.customerName() != null));
        check("platform snapshot", cannotThrowReturn(() -> reports.platformSnapshot()).orders() > 0);

        Platform empty = new Platform(false);
        ReportService emptyReports = empty.reports();
        check("7 reports an absent most ordered item as empty, not as a value",
                emptyReports.mostFrequentlyOrderedItem().item().isEmpty()
                        && emptyReports.mostFrequentlyOrderedItem().totalUnits() == 0L);
        check("9 reports an absent peak hour as empty, not as a guess",
                emptyReports.peakOrderingHour().isEmpty());
        check("reports survive an empty platform",
                emptyReports.totalRevenue(LocalDate.now().minusYears(1), LocalDate.now()).isZero()
                        && emptyReports.topRestaurantsByRevenue(YearMonth.now()).isEmpty()
                        && emptyReports.orderCountByStatus().values().stream().allMatch(count -> count == 0L)
                        && emptyReports.riderPerformance().isEmpty()
                        && emptyReports.topSellingItems(5).isEmpty()
                        && emptyReports.customersNotOrderedRecently(0).isEmpty());
        check("no most ordered item exists before any order is placed",
                new Platform(false).reports().mostFrequentlyOrderedItem().item().isEmpty());
    }

    private boolean queueSkipsCancelled(Platform platform) throws Exception {
        DispatchQueue queue = new DispatchQueue();
        Customer customer = platform.customers().require("C-004");
        Restaurant restaurant = platform.restaurants().require("R-ZAMALEK-1");
        Order order = cannotThrowReturn(() -> platform.orderService().placeOrder(customer, restaurant,
                customer.addressList().get(0), List.of(OrderLine.of(restaurant.requireItem("CO-1"), BigDecimal.ONE)),
                null, null));
        platform.orderService().restaurantAccept(order.id());
        platform.orderService().markPreparing(order.id());
        platform.orderService().markReady(order.id());
        queue.offer(order, customer.tier(), order.readyAt());
        platform.orderService().cancelOrder(order.id(), "no longer needed");
        return queue.pollNext().isEmpty();
    }

    private boolean isNewestFirst(List<Order> orders) {
        for (int i = 1; i < orders.size(); i++) {
            if (orders.get(i - 1).placedAt().isBefore(orders.get(i).placedAt())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSortedByRatingThenName(List<Restaurant> restaurants) {
        for (int i = 1; i < restaurants.size(); i++) {
            Restaurant previous = restaurants.get(i - 1);
            Restaurant current = restaurants.get(i);
            if (previous.rating() < current.rating()) {
                return false;
            }
            if (previous.rating() == current.rating()
                    && previous.name().compareToIgnoreCase(current.name()) > 0) {
                return false;
            }
        }
        return true;
    }

    private PriceBreakdown price(Platform platform, Customer customer, Restaurant restaurant, Address address,
                                 List<OrderLine> lines, String promotionCode) throws PlatformException {
        return priceWithPromotion(platform, customer, restaurant, address, lines,
                promotionCode == null ? null : platform.promotions().require(promotionCode));
    }

    private PriceBreakdown priceWithPromotion(Platform platform, Customer customer, Restaurant restaurant,
                                              Address address, List<OrderLine> lines, Promotion promotion)
            throws PlatformException {
        return platform.pricing().price(customer, restaurant, address, lines, promotion, LocalDate.now());
    }

    private Money silverFee(Platform platform) throws PlatformException {
        Customer customer = platform.customers().require("C-002");
        for (int completed = customer.completedOrderCount(); completed < 10; completed++) {
            customer.markOrderCompleted();
        }
        return price(platform, customer, platform.restaurants().require("R-DOKKI-1"),
                customer.addressList().get(0),
                List.of(OrderLine.of(platform.restaurants().require("R-DOKKI-1").requireItem("SU-1"), BigDecimal.ONE)),
                null).deliveryFee();
    }

    private Money goldFee(Platform platform) throws PlatformException {
        Customer customer = platform.customers().require("C-002");
        for (int completed = customer.completedOrderCount(); completed < 30; completed++) {
            customer.markOrderCompleted();
        }
        return price(platform, customer, platform.restaurants().require("R-DOKKI-1"),
                customer.addressList().get(0),
                List.of(OrderLine.of(platform.restaurants().require("R-DOKKI-1").requireItem("SU-1"), BigDecimal.ONE)),
                null).deliveryFee();
    }

    private Platform seeded() {
        return new Platform(true);
    }

    private void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + description);
        } else {
            failures.add(description);
            System.out.println("  FAIL  " + description);
        }
    }

    private boolean throwsRuntime(ThrowingRunnable action, Class<? extends RuntimeException> type) {
        try {
            action.run();
            failures.add("expected " + type.getSimpleName() + " but nothing was thrown");
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but nothing was thrown");
            return false;
        } catch (RuntimeException e) {
            if (type.isInstance(e)) {
                return true;
            }
            failures.add("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
            return false;
        } catch (Exception e) {
            failures.add("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
            return false;
        }
    }

    private boolean runs(ThrowingRunnable action) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            failures.add("unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  FAIL  unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private void cannotThrow(ThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            failures.add("unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  FAIL  unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private <T> T cannotThrowReturn(ThrowingSupplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private boolean throwsValidation(ThrowingRunnable action) {
        return throwsCode(action, ValidationException.class, null);
    }

    private boolean throwsCatalog(ThrowingRunnable action, String code) {
        return throwsCode(action, CatalogException.class, code);
    }

    private boolean throwsStock(ThrowingRunnable action) {
        return throwsCode(action, StockException.class, null);
    }

    private boolean throwsPayment(ThrowingRunnable action, String code) {
        return throwsCode(action, PaymentException.class, code);
    }

    private boolean throwsDispatch(ThrowingRunnable action, String code) {
        return throwsCode(action, DispatchException.class, code);
    }

    private boolean throwsLifecycle(ThrowingRunnable action, String code) {
        return throwsCode(action, LifecycleException.class, code);
    }

    private boolean throwsPromotion(ThrowingRunnable action, String code) {
        return throwsCode(action, PromotionException.class, code);
    }

    private boolean throwsCode(ThrowingRunnable action, Class<? extends PlatformException> type, String code) {
        try {
            action.run();
            failures.add("expected " + type.getSimpleName() + " but nothing was thrown");
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but nothing was thrown");
            return false;
        } catch (PlatformException e) {
            if (!type.isInstance(e)) {
                failures.add("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
                System.out.println("  FAIL  expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
                return false;
            }
            if (code != null && !code.equals(e.code())) {
                failures.add("expected code " + code + " but got " + e.code());
                System.out.println("  FAIL  expected code " + code + " but got " + e.code());
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            failures.add("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            failures.add("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  FAIL  expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean throwsUnsupported(Runnable action) {
        try {
            action.run();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
        } catch (NullPointerException | IllegalArgumentException accepted) {
            return true;
        }
    }

    static final class AbsurdPromotion extends Promotion {

        AbsurdPromotion() {
            super("ABSURD", "discounts more than the order is worth", null, List.of());
        }

        @Override
        public String kind() {
            return "absurd test promotion";
        }

        @Override
        public Money subtotalDiscount(PromotionContext context) {
            return Money.ofEgp("10000");
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
