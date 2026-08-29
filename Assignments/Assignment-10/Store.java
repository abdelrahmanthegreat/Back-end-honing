import java.util.*;

public class Store {
    private final List<Product> products = new ArrayList<>();
    private final Map<Integer, Product> productById = new HashMap<>();
    private final Set<String> categories = new LinkedHashSet<>();

    private final Map<Integer, Order> orderRecord = new HashMap<>();
    private final Deque<Order> shippingList = new ArrayDeque<>();
    private final Set<Integer> shippingOrderIds = new HashSet<>();
    private final Map<Integer, Order> deliveredOrders = new LinkedHashMap<>();

    private final List<Review> reviews = new ArrayList<>();

    public String addProduct(int id, String name, double price, String category, int stockQuantity) {
        try {
            if (productById.containsKey(id)) {
                return "A product with ID " + id + " already exists.";
            }

            Product product = new Product(id, name, price, category, stockQuantity);
            products.add(product);
            productById.put(id, product);
            categories.add(product.getCategory());

            return "Product added successfully.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String removeProduct(int id) {
        if (!productById.containsKey(id)) {
            return "Product ID " + id + " not found.";
        }

        deleteProductEverywhere(id);
        return "Product removed successfully.";
    }

    private void deleteProductEverywhere(int id) {
        Product removed = productById.remove(id);
        if (removed != null) {
            products.remove(removed);
            refreshCategories();
        }
    }

    private void refreshCategories() {
        categories.clear();
        for (Product product : products) {
            categories.add(product.getCategory());
        }
    }

    public void displayAllProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        System.out.println("Products in insertion order:");
        for (Product product : products) {
            System.out.println(product);
        }
    }

    public void searchProduct(int id) {
        Product product = productById.get(id);
        if (product == null) {
            System.out.println("Product ID " + id + " not found.");
        } else {
            System.out.println("Product found:");
            System.out.println(product);
        }
    }

    public void displayCategories() {
        if (categories.isEmpty()) {
            System.out.println("No categories available.");
            return;
        }

        System.out.println("Categories:");
        int index = 1;
        for (String category : categories) {
            System.out.println(index + ". " + category);
            index++;
        }
    }

    public void displayProductsByPrice() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        List<Product> sortedProducts = new ArrayList<>(products);
        Collections.sort(sortedProducts);

        System.out.println("Products by price, from cheapest to most expensive:");
        for (Product product : sortedProducts) {
            System.out.println(product);
        }
    }

    public String createOrder(int orderId, String customerName) {
        try {
            if (orderRecord.containsKey(orderId)) {
                return "Order ID " + orderId + " already exists.";
            }

            Order order = new Order(orderId, customerName);
            orderRecord.put(orderId, order);

            return "Order " + orderId + " created with status Pending.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String addItemToOrder(int orderId, int productId, int quantity) {
        Order order = orderRecord.get(orderId);
        if (order == null) {
            return "Order ID " + orderId + " not found.";
        }

        Product product = productById.get(productId);
        if (product == null) {
            return "Product ID " + productId + " does not exist in the shop.";
        }

        if (quantity <= 0) {
            return "Quantity must be greater than zero.";
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            return "Only Pending orders can be modified.";
        }

        try {
            order.addItem(product, quantity);
            return "Item added/updated in order " + orderId + ".";
        } catch (IllegalStateException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String removeItemFromOrder(int orderId, int productId) {
        Order order = orderRecord.get(orderId);
        if (order == null) {
            return "Order ID " + orderId + " not found.";
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            return "Only Pending orders can be modified.";
        }

        boolean removed = order.removeItem(productId);
        if (!removed) {
            return "No item with product ID " + productId + " found in order " + orderId + ".";
        }

        return "Item removed from order " + orderId + ".";
    }

    public void displayOrder(int orderId) {
        Order order = orderRecord.get(orderId);
        if (order == null) {
            System.out.println("Order ID " + orderId + " not found.");
            return;
        }

        order.displayOrder();
    }

    public String addOrderToShippingList(int orderId) {
        Order order = orderRecord.get(orderId);
        if (order == null) {
            return "Order ID " + orderId + " not found.";
        }

        switch (order.getStatus()) {
            case PENDING:
                if (!order.hasItems()) {
                    return "This order has no items and cannot be shipped.";
                }

                if (shippingOrderIds.contains(orderId)) {
                    return "Order " + orderId + " is already in the shipping list.";
                }

                shippingList.addLast(order);
                shippingOrderIds.add(orderId);
                order.updateStatus(OrderStatus.SHIPPED);

                return "Order " + orderId + " was added to the shipping list and marked Shipped.";

            case SHIPPED:
                return "Order " + orderId + " is already Shipped.";

            case DELIVERED:
                return "Order " + orderId + " has already been Delivered.";

            case CANCELLED:
                return "Order " + orderId + " is Cancelled and cannot be shipped.";

            default:
                return "Unknown order status.";
        }
    }

    public String shipNextOrder() {
        if (shippingList.isEmpty()) {
            return "The shipping list is empty.";
        }

        Order nextOrder = shippingList.peekFirst();
        if (nextOrder == null) {
            return "The shipping list is empty.";
        }

        if (!nextOrder.hasItems()) {
            return "This order has no items and cannot be shipped.";
        }

        if (nextOrder.getStatus() != OrderStatus.SHIPPED) {
            shippingList.pollFirst();
            shippingOrderIds.remove(nextOrder.getOrderId());
            return "Order " + nextOrder.getOrderId() + " cannot be shipped.";
        }

        shippingList.pollFirst();
        shippingOrderIds.remove(nextOrder.getOrderId());

        nextOrder.updateStatus(OrderStatus.DELIVERED);
        deliveredOrders.put(nextOrder.getOrderId(), nextOrder);

        return "Order " + nextOrder.getOrderId() + " was delivered.";
    }

    public String cancelOrder(int orderId) {
        Order order = orderRecord.get(orderId);
        if (order == null) {
            return "Order ID " + orderId + " not found.";
        }

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.DELIVERED) {
            return "Order " + orderId + " has already been delivered and cannot be cancelled.";
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            return "Order " + orderId + " is already cancelled.";
        }

        boolean wasShipped = currentStatus == OrderStatus.SHIPPED;

        shippingList.removeIf(orderInQueue -> orderInQueue.getOrderId() == orderId);
        shippingOrderIds.remove(orderId);

        order.updateStatus(OrderStatus.CANCELLED);

        if (wasShipped) {
            return "Order " + orderId + " was removed from the shipping list and cancelled.";
        }

        return "Order " + orderId + " was cancelled.";
    }

    public void searchOrder(int orderId) {
        displayOrder(orderId);
    }

    public String addReview(int productId, String customerName, String comment) {
        if (!productById.containsKey(productId)) {
            return "Review cannot be added because product ID " + productId + " does not exist.";
        }

        try {
            Review review = new Review(productId, customerName, comment);
            reviews.add(review);
            return "Review added successfully.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public void showReviewsForProduct(int productId) {
        System.out.println("Reviews for product ID " + productId + ":");

        boolean found = false;
        for (Review review : reviews) {
            if (review.getProductId() == productId) {
                System.out.println(review);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No reviews found.");
        }
    }

    public String removeOutOfStockProducts() {
        int removedCount = 0;
        Iterator<Product> iterator = products.iterator();

        while (iterator.hasNext()) {
            Product product = iterator.next();

            if (product.getStockQuantity() == 0) {
                iterator.remove();
                deleteProductEverywhere(product.getId());
                removedCount++;
            }
        }

        if (removedCount == 0) {
            return "No out-of-stock products found.";
        }

        return removedCount + " out-of-stock product(s) removed.";
    }

    public void displayOrdersByTotal() {
        if (orderRecord.isEmpty()) {
            System.out.println("No orders available.");
            return;
        }

        List<Order> sortedOrders = new ArrayList<>(orderRecord.values());
        Collections.sort(sortedOrders, new OrderTotalComparator());

        System.out.println("Orders by total, from lowest to highest:");
        for (Order order : sortedOrders) {
            System.out.printf(
                    Locale.US,
                    "Order #%d | Customer: %s | Status: %s | Total: %.2f%n",
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getStatus(),
                    order.getTotal()
            );
        }
    }
}