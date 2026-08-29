public class Main {
    public static void main(String[] args) {
        Store store = new Store();
        InputReader input = new InputReader();

        System.out.println("E-Commerce Order & Inventory Manager");

        try {
            while (true) {
                printMenu();

                int choice = input.readInt("Enter your choice (1-19): ", 1, 19);

                switch (choice) {
                    case 1:
                        addProduct(store, input);
                        break;
                    case 2:
                        removeProduct(store, input);
                        break;
                    case 3:
                        store.displayAllProducts();
                        break;
                    case 4:
                        searchProduct(store, input);
                        break;
                    case 5:
                        store.displayCategories();
                        break;
                    case 6:
                        store.displayProductsByPrice();
                        break;
                    case 7:
                        createOrder(store, input);
                        break;
                    case 8:
                        addItemToOrder(store, input);
                        break;
                    case 9:
                        removeItemFromOrder(store, input);
                        break;
                    case 10:
                        displayOrder(store, input);
                        break;
                    case 11:
                        addOrderToShippingList(store, input);
                        break;
                    case 12:
                        System.out.println(store.shipNextOrder());
                        break;
                    case 13:
                        cancelOrder(store, input);
                        break;
                    case 14:
                        searchOrder(store, input);
                        break;
                    case 15:
                        addReview(store, input);
                        break;
                    case 16:
                        showReviews(store, input);
                        break;
                    case 17:
                        System.out.println(store.removeOutOfStockProducts());
                        break;
                    case 18:
                        store.displayOrdersByTotal();
                        break;
                    case 19:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }

                System.out.println();
            }
        } catch (IllegalStateException e) {
            System.out.println("\n" + e.getMessage() + " Exiting.");
        }
    }

    private static void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("1. Add Product");
        System.out.println("2. Remove Product");
        System.out.println("3. Display All Products");
        System.out.println("4. Search Product by ID");
        System.out.println("5. Show All Categories");
        System.out.println("6. Display Products Ordered by Price");
        System.out.println("7. Create Order");
        System.out.println("8. Add Item to Order");
        System.out.println("9. Remove Item from Order");
        System.out.println("10. Display Order");
        System.out.println("11. Add Order to the Shipping List");
        System.out.println("12. Ship Next Order");
        System.out.println("13. Cancel Order");
        System.out.println("14. Search Order by ID");
        System.out.println("15. Add Review to a Product");
        System.out.println("16. Show All Reviews for a Product");
        System.out.println("17. Remove Out-of-Stock Products");
        System.out.println("18. Display Orders Ordered by Total");
        System.out.println("19. Exit");
        System.out.println("==========================");
    }

    private static void addProduct(Store store, InputReader input) {
        int id = input.readInt("Product ID: ", 1);
        String name = input.readNonEmpty("Product name: ");
        double price = input.readDouble("Price: ", 0.0);
        String category = input.readNonEmpty("Category: ");
        int stockQuantity = input.readInt("Stock quantity: ", 0);

        System.out.println(store.addProduct(id, name, price, category, stockQuantity));
    }

    private static void removeProduct(Store store, InputReader input) {
        int id = input.readInt("Product ID to remove: ", 1);
        System.out.println(store.removeProduct(id));
    }

    private static void searchProduct(Store store, InputReader input) {
        int id = input.readInt("Product ID to search: ", 1);
        store.searchProduct(id);
    }

    private static void createOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID: ", 1);
        String customerName = input.readNonEmpty("Customer name: ");

        System.out.println(store.createOrder(orderId, customerName));
    }

    private static void addItemToOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID: ", 1);
        int productId = input.readInt("Product ID: ", 1);
        int quantity = input.readInt("Quantity: ", 1);

        System.out.println(store.addItemToOrder(orderId, productId, quantity));
    }

    private static void removeItemFromOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID: ", 1);
        int productId = input.readInt("Product ID to remove: ", 1);

        System.out.println(store.removeItemFromOrder(orderId, productId));
    }

    private static void displayOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID to display: ", 1);
        store.displayOrder(orderId);
    }

    private static void addOrderToShippingList(Store store, InputReader input) {
        int orderId = input.readInt("Order ID to add to shipping list: ", 1);
        System.out.println(store.addOrderToShippingList(orderId));
    }

    private static void cancelOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID to cancel: ", 1);
        System.out.println(store.cancelOrder(orderId));
    }

    private static void searchOrder(Store store, InputReader input) {
        int orderId = input.readInt("Order ID to search: ", 1);
        store.searchOrder(orderId);
    }

    private static void addReview(Store store, InputReader input) {
        int productId = input.readInt("Product ID to review: ", 1);
        String customerName = input.readNonEmpty("Customer name: ");
        String comment = input.readNonEmpty("Comment: ");

        System.out.println(store.addReview(productId, customerName, comment));
    }

    private static void showReviews(Store store, InputReader input) {
        int productId = input.readInt("Product ID to show reviews for: ", 1);
        store.showReviewsForProduct(productId);
    }
}