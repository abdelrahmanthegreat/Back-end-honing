import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    addMenuItem(restaurant);
                    break;
                case 2:
                    removeMenuItem(restaurant);
                    break;
                case 3:
                    restaurant.displayMenu();
                    break;
                case 4:
                    searchMenuItem(restaurant);
                    break;
                case 5:
                    createOrder(restaurant);
                    break;
                case 6:
                    addItemToOrder(restaurant);
                    break;
                case 7:
                    removeItemFromOrder(restaurant);
                    break;
                case 8:
                    displayOrder(restaurant);
                    break;
                case 9:
                    addOrderToKitchenQueue(restaurant);
                    break;
                case 10:
                    restaurant.processNextOrder();
                    break;
                case 11:
                    searchOrder(restaurant);
                    break;
                case 12:
                    checkOrderStatus(restaurant);
                    break;
                case 13:
                    restaurant.displayCompletedOrders();
                    break;
                case 14:
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== Restaurant Order Manager =====");
        System.out.println("1. Add Menu Item");
        System.out.println("2. Remove Menu Item");
        System.out.println("3. Display Menu");
        System.out.println("4. Search Menu Item");
        System.out.println("5. Create Order");
        System.out.println("6. Add Item to Order");
        System.out.println("7. Remove Item from Order");
        System.out.println("8. Display Order");
        System.out.println("9. Add Order to Kitchen Queue");
        System.out.println("10. Process Next Order");
        System.out.println("11. Search Order");
        System.out.println("12. Check Order Status");
        System.out.println("13. Display Completed Orders");
        System.out.println("14. Exit");
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter an integer: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static void addMenuItem(Restaurant restaurant) {
        int id = readInt("Enter menu item ID: ");
        String name = readString("Enter name: ");
        double price = readDouble("Enter price: ");
        String category = readString("Enter category: ");
        restaurant.addMenuItem(id, name, price, category);
    }

    private static void removeMenuItem(Restaurant restaurant) {
        int id = readInt("Enter menu item ID to remove: ");
        restaurant.removeMenuItem(id);
    }

    private static void searchMenuItem(Restaurant restaurant) {
        int id = readInt("Enter menu item ID to search: ");
        restaurant.searchMenuItem(id);
    }

    private static void createOrder(Restaurant restaurant) {
        int orderId = readInt("Enter order ID: ");
        String customerName = readString("Enter customer name: ");
        restaurant.createOrder(orderId, customerName);
    }

    private static void addItemToOrder(Restaurant restaurant) {
        int orderId = readInt("Enter order ID: ");
        int menuItemId = readInt("Enter menu item ID: ");
        int quantity = readInt("Enter quantity: ");
        restaurant.addItemToOrder(orderId, menuItemId, quantity);
    }

    private static void removeItemFromOrder(Restaurant restaurant) {
        int orderId = readInt("Enter order ID: ");
        int menuItemId = readInt("Enter menu item ID to remove: ");
        restaurant.removeItemFromOrder(orderId, menuItemId);
    }

    private static void displayOrder(Restaurant restaurant) {
        int orderId = readInt("Enter order ID to display: ");
        restaurant.displayOrder(orderId);
    }

    private static void addOrderToKitchenQueue(Restaurant restaurant) {
        int orderId = readInt("Enter order ID to add to kitchen queue: ");
        restaurant.addOrderToKitchenQueue(orderId);
    }

    private static void searchOrder(Restaurant restaurant) {
        int orderId = readInt("Enter order ID to search: ");
        restaurant.searchOrder(orderId);
    }

    private static void checkOrderStatus(Restaurant restaurant) {
        int orderId = readInt("Enter order ID to check status: ");
        restaurant.checkOrderStatus(orderId);
    }
}