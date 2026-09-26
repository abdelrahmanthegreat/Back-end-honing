package masr;

import java.math.BigDecimal;
import java.util.List;

public final class SeedData {

    private SeedData() {
    }

    public static void install(Platform platform) {
        try {
            seedRestaurants(platform);
            seedCustomers(platform);
            seedRiders(platform);
            seedPromotions(platform);
            seedOrders(platform);
        } catch (PlatformException e) {
            throw new IllegalStateException("Demo data could not be installed: " + e.getMessage(), e);
        }
    }

    private static void seedRestaurants(Platform platform) throws PlatformException {
        Restaurant nileGrill = restaurant(platform, "R-MAADI-1", "Nile Grill House", District.MAADI,
                List.of(Cuisine.EGYPTIAN, Cuisine.GRILL), 4.6d, true,
                "WEIGHTED|MG-1|Mixed Grill Platter|240.00|Grill|25|true|40|",
                "STANDARD|ST-1|Grilled Kofta Sandwich|95.00|Sandwiches|12|true|60|",
                "STANDARD|ST-2|Ful Medames|45.00|Breakfast|8|true|80|",
                "STANDARD|ST-3|Basmati Rice|35.00|Sides|6|true|100|",
                "WEIGHTED|WG-1|Charcoal Kofta|260.00|Grill|20|true|30|",
                "STANDARD|SW-1|Fresh Orange Juice|30.00|Drinks|3|false|50|",
                "COMBO|CB-1|Nile Grill Combo|320.00|Combos|25|true|25|ST-1;MG-1");

        restaurant(platform, "R-FAISAL-1", "Faisal Street Pizza", District.FAISAL,
                List.of(Cuisine.PIZZA, Cuisine.ITALIAN), 4.2d, true,
                "STANDARD|PZ-1|Margherita Pizza|120.00|Pizza|15|true|50|",
                "STANDARD|PZ-2|Tuna Pizza|165.00|Pizza|15|true|50|",
                "STANDARD|PA-1|Pasta Arrabbiata|110.00|Pasta|14|true|40|",
                "STANDARD|PL-1|Lemon Mint|25.00|Drinks|2|true|60|",
                "COMBO|PC-1|Pizza Night Combo|250.00|Combos|18|true|20|PZ-1;PZ-2");

        restaurant(platform, "R-DOKKI-1", "Dokki Sushi Bar", District.DOKKI,
                List.of(Cuisine.JAPANESE, Cuisine.SUSHI), 4.8d, true,
                "STANDARD|SU-1|Salmon Nigiri|95.00|Nigiri|10|true|60|",
                "STANDARD|SU-2|Tuna Maki|70.00|Maki|10|true|60|",
                "COMBO|SC-1|Sushi Platter For Two|155.00|Combos|15|true|15|SU-1;SU-2",
                "WEIGHTED|SW-2|Fresh Tuna Fillet|580.00|Fish|18|true|20|");

        restaurant(platform, "R-ZAMALEK-1", "Zamalek Coffee House", District.ZAMALEK,
                List.of(Cuisine.COFFEE, Cuisine.DESSERTS), 3.9d, true,
                "STANDARD|CO-1|Flat White|55.00|Coffee|4|true|100|",
                "STANDARD|DE-1|Basbousa|75.00|Desserts|10|true|40|",
                "STANDARD|DE-2|Kunafa|90.00|Desserts|12|true|35|",
                "COMBO|DC-1|Coffee And Basbousa|115.00|Combos|12|true|30|CO-1;DE-1");

        restaurant(platform, "R-GIZA-1", "Giza Koshary House", District.GIZA,
                List.of(Cuisine.EGYPTIAN, Cuisine.STREET_FOOD), 4.4d, false,
                "STANDARD|KO-1|Koshary|45.00|Local|10|true|90|",
                "STANDARD|KO-2|Fuul Medames|50.00|Local|10|true|70|");

        restaurant(platform, "R-NASR-1", "Nasr City Shawarma", District.NASR_CITY,
                List.of(Cuisine.STREET_FOOD, Cuisine.FAST_FOOD), 4.1d, true,
                "STANDARD|SH-1|Shawarma Platter|135.00|Shawarma|12|true|70|",
                "STANDARD|SH-3|Fries|40.00|Sides|7|true|100|",
                "STANDARD|SH-4|Ades|15.00|Drinks|2|true|120|",
                "COMBO|SH-2|Shawarma Family Combo|160.00|Combos|15|true|25|SH-1;SH-3");

        restaurant(platform, "R-HELIO-1", "Heliopolis Fine Dining", District.HELIOPOLIS,
                List.of(Cuisine.ITALIAN, Cuisine.SEAFOOD), 4.9d, true,
                "STANDARD|HD-1|Grilled Salmon Fillet|340.00|Mains|20|true|30|",
                "STANDARD|HD-3|Caprese Salad|160.00|Starters|10|true|40|",
                "WEIGHTED|HD-4|Prime Ribeye|890.00|Meats|25|true|15|",
                "COMBO|HD-2|Dinner For Two|450.00|Combos|25|true|12|HD-1;HD-3");

        nileGrill.rate(4.8d);
    }

    private static Restaurant restaurant(Platform platform, String id, String name, District district,
                                         List<Cuisine> cuisines, double rating, boolean open, String... menuRows)
            throws PlatformException {
        Restaurant restaurant = new Restaurant(id, name, district, cuisines, rating, open);
        for (String row : menuRows) {
            MenuItem item = MenuItemFactory.create(MenuItemSpec.fromCsvLine(row), restaurant);
            restaurant.addMenuItem(item);
        }
        return platform.restaurants().add(restaurant);
    }

    private static void seedCustomers(Platform platform) throws PlatformException {
        customer(platform, "C-001", "Ahmed Hassan", "01012345678", "500.00",
                new String[]{"MAADI|12 El Nasr Street, Building 3, Flat 7", "FAISAL|45 Freedom Street, Flat 2"});
        customer(platform, "C-002", "Mona Khalil", "01123456789", "800.00",
                new String[]{"MAADI|88 Corniche, Tower A, Flat 12"});
        customer(platform, "C-003", "Youssef Nabil", "01234567890", "300.00",
                new String[]{"DOKKI|3 Tahrir Street, Office 5"});
        customer(platform, "C-004", "Sara Emad", "01598765432", "600.00",
                new String[]{"ZAMALEK|22 Brazil Street, Flat 3"});
    }

    private static Customer customer(Platform platform, String id, String name, String mobile, String balance,
                                     String[] addresses) throws PlatformException {
        Customer customer = new Customer(id, name, mobile, Money.ofEgp(balance));
        platform.customers().add(customer);
        for (String address : addresses) {
            String[] parts = address.split("\\|", 2);
            customer.addAddress(District.parse(parts[0]), parts[1]);
        }
        return customer;
    }

    private static void seedRiders(Platform platform) throws PlatformException {
        Rider karim = new Rider("RD-1", "Karim Fouad", VehicleType.MOTORCYCLE, District.MAADI);
        Rider nada = new Rider("RD-2", "Nada Ashraf", VehicleType.BICYCLE, District.DOKKI);
        Rider hassan = new Rider("RD-3", "Hassan Mahmoud", VehicleType.CAR, District.NASR_CITY);
        platform.riders().add(karim);
        platform.riders().add(nada);
        platform.riders().add(hassan);
        karim.goOnDuty();
        nada.goOnDuty();
        hassan.goOnDuty();
    }

    private static void seedPromotions(Platform platform) throws PlatformException {
        platform.promotions().register(PercentageOffPromotion.of("NILE20",
                new BigDecimal("20"), Money.ofEgp("50.00"),
                java.time.LocalDate.of(2027, 12, 31),
                PromotionCondition.minimumSubtotal(Money.ofEgp("200.00"))));

        platform.promotions().register(FixedAmountPromotion.of("WELCOME15",
                Money.ofEgp("15.00"), java.time.LocalDate.of(2027, 12, 31),
                PromotionCondition.firstTimeCustomersOnly()));

        platform.promotions().register(FreeDeliveryPromotion.of("FREEGO",
                java.time.LocalDate.of(2027, 12, 31),
                PromotionCondition.minimumSubtotal(Money.ofEgp("150.00"))));

        platform.promotions().register(FixedAmountPromotion.of("OLDGOLD",
                Money.ofEgp("50.00"), java.time.LocalDate.of(2020, 1, 1)));

        platform.promotions().register(PercentageOffPromotion.of("GIZAONLY",
                new BigDecimal("30"), Money.ofEgp("90.00"),
                java.time.LocalDate.of(2027, 12, 31),
                PromotionCondition.districtAllowed(District.GIZA)));
    }

    private static void seedOrders(Platform platform) throws PlatformException {
        OrderService service = platform.orderService();
        Restaurant nileGrill = platform.restaurants().require("R-MAADI-1");
        Restaurant sushi = platform.restaurants().require("R-DOKKI-1");
        Restaurant shawarma = platform.restaurants().require("R-NASR-1");
        Restaurant coffee = platform.restaurants().require("R-ZAMALEK-1");

        Customer ahmed = platform.customers().require("C-001");
        Customer mona = platform.customers().require("C-002");
        Customer youssef = platform.customers().require("C-003");
        Customer sara = platform.customers().require("C-004");

        Address ahmedFaisal = ahmed.findAddress("Freedom").orElseThrow();
        Address monaMaadi = mona.findAddress("Corniche").orElseThrow();
        Address youssefDokki = youssef.findAddress("Tahrir").orElseThrow();
        Address saraZamalek = sara.findAddress("Brazil").orElseThrow();

        Order workedExample = service.placeOrder(ahmed, nileGrill, ahmedFaisal,
                List.of(OrderLine.of(nileGrill.requireItem("MG-1"), new BigDecimal("1.0"))),
                "NILE20", "extra grilled onions please");
        service.payFromWallet(workedExample.id());
        service.restaurantAccept(workedExample.id());
        service.markPreparing(workedExample.id());
        service.markReady(workedExample.id());
        service.dispatchNext();

        Order delivered = service.placeOrder(mona, sushi, monaMaadi,
                List.of(OrderLine.of(sushi.requireItem("SU-1"), new BigDecimal("2")),
                        OrderLine.of(sushi.requireItem("SC-1"), new BigDecimal("1"))),
                null, null);
        service.payFromWallet(delivered.id());
        service.restaurantAccept(delivered.id());
        service.markPreparing(delivered.id());
        service.markReady(delivered.id());
        service.dispatchNext();
        service.markPickedUp(delivered.id());
        service.markDelivered(delivered.id());

        Order pending = service.placeOrder(youssef, shawarma, youssefDokki,
                List.of(OrderLine.of(shawarma.requireItem("SH-2"), new BigDecimal("1"))),
                null, "no pickles");
        service.restaurantAccept(pending.id());

        service.placeOrder(sara, coffee, saraZamalek,
                List.of(OrderLine.of(coffee.requireItem("DC-1"), new BigDecimal("2"))),
                null, null);
    }
}
