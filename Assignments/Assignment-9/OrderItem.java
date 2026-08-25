public class OrderItem {
    private MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double calculateSubtotal() {
        return item.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return item.getName() + " x " + quantity + " = " + calculateSubtotal();
    }
}