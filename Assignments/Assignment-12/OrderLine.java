package masr;

import java.math.BigDecimal;
import java.util.Objects;

public final class OrderLine {

    private final MenuItem item;
    private final BigDecimal quantity;
    private final Money unitPriceSnapshot;

    public OrderLine(MenuItem item, BigDecimal quantity, Money unitPriceSnapshot) {
        this.item = Objects.requireNonNull(item, "item must not be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
        this.unitPriceSnapshot = Objects.requireNonNull(unitPriceSnapshot, "unitPriceSnapshot must not be null");
    }

    public static OrderLine of(MenuItem item, BigDecimal quantity) {
        return new OrderLine(item, quantity, item.unitPrice());
    }

    public MenuItem item() {
        return item;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public Money unitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public Money lineTotal() {
        return unitPriceSnapshot.times(quantity);
    }

    public int units() {
        return item.unitsFor(quantity);
    }

    public String quantityLabel() {
        return quantity.stripTrailingZeros().toPlainString() + " " + item.quantityUnit();
    }

    public String describe() {
        return quantityLabel() + " x " + item.name() + " @ " + unitPriceSnapshot.format() + " EGP = " + lineTotal().format() + " EGP";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof OrderLine other && item.id().equals(other.item.id());
    }

    @Override
    public int hashCode() {
        return item.id().hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }
}
