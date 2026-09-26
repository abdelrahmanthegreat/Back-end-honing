package masr;

import java.util.List;
import java.util.function.Function;

public final class MenuSupport {

    private final ConsoleIo io;
    private final Platform platform;

    public MenuSupport(ConsoleIo io, Platform platform) {
        this.io = io;
        this.platform = platform;
    }

    public ConsoleIo io() {
        return io;
    }

    public Platform platform() {
        return platform;
    }

    public <T> T pick(String title, List<T> options, Function<T, String> display) {
        if (options.isEmpty()) {
            io.error("There is nothing to choose from: " + title + " is empty.");
            return null;
        }
        io.println();
        io.println(title);
        for (int i = 0; i < options.size(); i++) {
            io.println(String.format("  %2d. %s", i + 1, display.apply(options.get(i))));
        }
        int choice = io.readIntOrDefault("Choose 1-" + options.size() + " (0 or enter to go back): ", 0) - 1;
        if (choice < -1 || choice >= options.size()) {
            io.warn("Please choose a number between 1 and " + options.size() + ".");
            return pick(title, options, display);
        }
        if (choice < 0) {
            return null;
        }
        return options.get(choice);
    }

    public Customer pickCustomer() {
        List<Customer> customers = platform.customers().all();
        if (customers.isEmpty()) {
            io.error("No customers are registered yet.");
            return null;
        }
        return pick("CUSTOMERS", customers, Customer::describe);
    }

    public Restaurant pickRestaurant() {
        List<Restaurant> restaurants = platform.restaurants().byRating();
        if (restaurants.isEmpty()) {
            io.error("No restaurants are registered yet.");
            return null;
        }
        return pick("RESTAURANTS (by rating)", restaurants, Restaurant::describe);
    }

    public Rider pickRider() {
        List<Rider> riders = platform.riders().all();
        if (riders.isEmpty()) {
            io.error("No riders are registered yet.");
            return null;
        }
        return pick("RIDERS", riders, Rider::describe);
    }

    public Order pickOrder(String title, List<Order> orders) {
        if (orders.isEmpty()) {
            io.error("There are no orders to show.");
            return null;
        }
        return pick(title, orders, Order::trackingLine);
    }

    public void run(String label, MenuAction action) {
        try {
            action.run();
        } catch (ConsoleIo.SessionEndedException ended) {
            throw ended;
        } catch (PlatformException e) {
            io.error(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            io.error(e.getMessage());
        }
    }

    public String askOrderReference() {
        return io.readRequired("Order reference (for example ORD-00001): ");
    }

    public void printMenu(String... lines) {
        io.println();
        for (String line : lines) {
            io.println("  " + line);
        }
    }

    @FunctionalInterface
    public interface MenuAction {
        void run() throws PlatformException;
    }
}
