import java.util.ArrayList;

public class Order {
    private int orderId;
    private String customerName;
    private ArrayList<OrderItem> items;
    private double total;
    private OrderStatus status;

    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = new ArrayList<>();
        this.total = 0.0;
        this.status = OrderStatus.PENDING;
    }

    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public ArrayList<OrderItem> getItems() { return items; }
    public double getTotal() { return total; }
    public OrderStatus getStatus() { return status; }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }

    public boolean removeItem(int menuItemId) {
        boolean removed = items.removeIf(item -> item.getItem().getId() == menuItemId);
        if (removed) {
            calculateTotal();
        }
        return removed;
    }

    public void calculateTotal() {
        total = 0.0;
        for (OrderItem item : items) {
            total += item.calculateSubtotal();
        }
    }

    public void displayOrder() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Customer: " + customerName);
        System.out.println("Status: " + status);
        System.out.println("Items:");
        if (items.isEmpty()) {
            System.out.println("  (no items)");
        } else {
            for (OrderItem item : items) {
                System.out.println("  " + item);
            }
        }
        System.out.println("Total: " + total);
    }
}