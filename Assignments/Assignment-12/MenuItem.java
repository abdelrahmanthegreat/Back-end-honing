package masr;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class MenuItem {

    private final String ownerRestaurantId;
    private final String id;
    private final String name;
    private final String category;
    private final int preparationMinutes;
    private boolean available;
    private int dailyStock;
    private int soldToday;

    protected MenuItem(String ownerRestaurantId, String id, String name, String category,
                      int preparationMinutes, boolean available, int dailyStock) {
        this.ownerRestaurantId = Objects.requireNonNull(ownerRestaurantId, "ownerRestaurantId must not be null");
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        if (preparationMinutes <= 0) {
            throw new IllegalArgumentException("preparationMinutes must be greater than zero");
        }
        if (dailyStock < 0) {
            throw new IllegalArgumentException("dailyStock must not be negative");
        }
        this.preparationMinutes = preparationMinutes;
        this.available = available;
        this.dailyStock = dailyStock;
    }

    public String ownerRestaurantId() {
        return ownerRestaurantId;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public int preparationMinutes() {
        return preparationMinutes;
    }

    public boolean isAvailable() {
        return available;
    }

    public int dailyStock() {
        return dailyStock;
    }

    public int soldToday() {
        return soldToday;
    }

    public int remainingStock() {
        return Math.max(0, dailyStock - soldToday);
    }

    public abstract ItemType type();

    public abstract Money unitPrice();

    public abstract Money priceFor(BigDecimal quantity);

    public abstract void validateQuantity(BigDecimal quantity) throws ValidationException;

    public abstract String quantityUnit();

    public abstract int unitsFor(BigDecimal quantity);

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setDailyStock(int dailyStock) {
        if (dailyStock < 0) {
            throw new IllegalArgumentException("dailyStock must not be negative");
        }
        this.dailyStock = dailyStock;
    }

    public void reserve(BigDecimal quantity) throws PlatformException {
        checkAvailable(quantity);
        soldToday += unitsFor(quantity);
    }

    public void checkAvailable(BigDecimal quantity) throws PlatformException {
        int units = unitsFor(quantity);
        validateQuantity(quantity);
        if (units > remainingStock()) {
            throw new StockException(StockException.OUT_OF_STOCK,
                    "'" + name + "' has " + remainingStock() + " " + quantityUnit() + " left today but " + units + " were requested");
        }
    }

    public void release(BigDecimal quantity) {
        soldToday = Math.max(0, soldToday - unitsFor(quantity));
    }

    public void startNewDay() {
        soldToday = 0;
    }

    protected final Money requirePositivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("price must be greater than zero");
        }
        return Money.ofEgp(price);
    }

    protected final void requirePositiveQuantity(BigDecimal quantity) throws ValidationException {
        if (quantity == null || quantity.signum() <= 0) {
            throw new ValidationException(ValidationException.INVALID_QUANTITY,
                    "quantity must be greater than zero");
        }
    }

    protected final void requireWholeQuantity(BigDecimal quantity) throws ValidationException {
        requirePositiveQuantity(quantity);
        if (quantity.stripTrailingZeros().scale() > 0) {
            throw new ValidationException(ValidationException.INVALID_QUANTITY,
                    "'" + name + "' is ordered by count, so a whole number is required but " + quantity.toPlainString() + " was given");
        }
    }

    public String describePrice() {
        return unitPrice().format() + " EGP per " + quantityUnit();
    }

    public String describeStock() {
        return remainingStock() + " " + quantityUnit() + " left today";
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MenuItem other)) {
            return false;
        }
        return ownerRestaurantId.equals(other.ownerRestaurantId) && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(ownerRestaurantId, id);
    }

    @Override
    public final String toString() {
        return name + " (" + type() + ", " + id + ", " + describePrice() + ")";
    }

    protected static String requireText(String value, String field) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }
}
