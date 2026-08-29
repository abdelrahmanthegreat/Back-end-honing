import java.util.Locale;

public class Product implements Comparable<Product> {
    private final int id;
    private String name;
    private double price;
    private String category;
    private int stockQuantity;

    public Product(int id, String name, double price, String category, int stockQuantity) {
        if (id <= 0) throw new IllegalArgumentException("Product ID must be positive.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty.");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("Category cannot be empty.");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative.");

        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.category = category.trim();
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty.");
        this.name = name.trim();
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("Category cannot be empty.");
        this.category = category.trim();
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative.");
        this.stockQuantity = stockQuantity;
    }

    @Override
    public int compareTo(Product other) {
        int result = Double.compare(this.price, other.price);
        if (result != 0) return result;
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "ID: %d | Name: %-15s | Price: %8.2f | Category: %-12s | Stock: %d",
                id, name, price, category, stockQuantity
        );
    }
}