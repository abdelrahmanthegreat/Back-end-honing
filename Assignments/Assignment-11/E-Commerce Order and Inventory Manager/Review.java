public class Review {
    private final int productId;
    private final String customerName;
    private final String comment;

    public Review(int productId, String customerName, String comment) {
        if (productId <= 0) throw new IllegalArgumentException("Product ID must be positive.");
        if (customerName == null || customerName.trim().isEmpty())
            throw new IllegalArgumentException("Customer name cannot be empty.");
        if (comment == null || comment.trim().isEmpty())
            throw new IllegalArgumentException("Comment cannot be empty.");

        this.productId = productId;
        this.customerName = customerName.trim();
        this.comment = comment.trim();
    }

    public int getProductId() {
        return productId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "Product ID: " + productId +
                " | Customer: " + customerName +
                " | Comment: " + comment;
    }
}