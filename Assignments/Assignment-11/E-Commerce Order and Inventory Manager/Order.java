import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Order {
    private final int orderId;
    private final String customerName;
    private final List<CartItem> items;
    private double total;
    private OrderStatus status;

    public Order(int orderId, String customerName) {
        if (orderId <= 0) throw new IllegalArgumentException("Order ID must be positive.");
        if (customerName == null || customerName.trim().isEmpty())
            throw new IllegalArgumentException("Customer name cannot be empty.");

        this.orderId = orderId;
        this.customerName = customerName.trim();
        this.items = new ArrayList<>();
        this.total = 0.0;
        this.status = OrderStatus.PENDING;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING)
            throw new IllegalStateException("Items can only be added to pending orders.");
        if (product == null)
            throw new IllegalArgumentException("Product cannot be null.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");

        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.increaseQuantity(quantity);
                calculateTotal();
                return;
            }
        }

        items.add(new CartItem(product, quantity));
        calculateTotal();
    }

    public boolean removeItem(int productId) {
        if (status != OrderStatus.PENDING)
            throw new IllegalStateException("Only pending orders can be modified.");

        boolean removed = items.removeIf(item -> item.getProduct().getId() == productId);
        calculateTotal();
        return removed;
    }

    public double calculateTotal() {
        double sum = 0.0;
        for (CartItem item : items) {
            sum += item.calculateSubtotal();
        }
        total = sum;
        return total;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Order status cannot be null.");
        if (!canTransitionTo(newStatus))
            throw new IllegalStateException("Cannot change order status from " + status + " to " + newStatus + ".");

        this.status = newStatus;
    }

    private boolean canTransitionTo(OrderStatus newStatus) {
        switch (status) {
            case PENDING:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED;
            default:
                return false;
        }
    }

    public void displayOrder() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Customer: " + customerName);
        System.out.println("Status: " + status);

        if (items.isEmpty()) {
            System.out.println("No items.");
        } else {
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                System.out.printf(
                        Locale.US,
                        "%d. %s x %d = %.2f%n",
                        i + 1,
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.calculateSubtotal()
                );
            }
        }

        System.out.printf(Locale.US, "Total: %.2f%n", total);
    }
}