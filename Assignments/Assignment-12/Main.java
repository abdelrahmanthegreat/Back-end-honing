package masr;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConsoleIo io = new ConsoleIo();
        Platform platform;
        try {
            platform = new Platform();
        } catch (RuntimeException e) {
            io.error("The platform could not start: " + e.getMessage());
            return;
        }
        MenuSupport menu = new MenuSupport(io, platform);
        CustomerMenu customerMenu = new CustomerMenu(io, menu);
        RestaurantMenu restaurantMenu = new RestaurantMenu(io, menu);
        RiderMenu riderMenu = new RiderMenu(io, menu);
        AdminMenu adminMenu = new AdminMenu(io, menu);

        io.println();
        io.println("============================================");
        io.println("MASR DELIVERY");
        io.println("============================================");
        io.success("Loaded " + platform.restaurants().size() + " restaurants, "
                + platform.customers().size() + " customers, " + platform.riders().size() + " riders, "
                + platform.orders().size() + " orders and " + platform.promotions().size() + " promotions.");
        io.info("Configuration: base fee " + platform.config().baseDeliveryFee()
                + " + " + platform.config().perExtraKmFee() + " per extra km after "
                + platform.config().includedKilometres() + " km, service fee "
                + platform.config().serviceFeeRate().multiply(new java.math.BigDecimal("100")).toPlainString() + "%");

        try {
            while (true) {
                io.banner("MASR DELIVERY - Main Menu");
                io.println("1. Customer");
                io.println("2. Restaurant");
                io.println("3. Rider");
                io.println("4. Admin & Reports");
                io.println("0. Exit");
                int choice = io.readChoice("Choose an option: ", 5);
                switch (choice) {
                    case 0 -> {
                        io.println();
                        io.println("Goodbye.");
                        return;
                    }
                    case 1 -> customerMenu.loop();
                    case 2 -> restaurantMenu.loop();
                    case 3 -> riderMenu.loop();
                    case 4 -> adminMenu.loop();
                    default -> io.warn("Unknown option.");
                }
            }
        } catch (ConsoleIo.SessionEndedException ended) {
            io.println();
            io.println("Input stream closed, goodbye.");
        }
    }
}
