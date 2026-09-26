package masr;

import java.math.BigDecimal;

public final class StandardItem extends MenuItem {

    private final Money price;

    public StandardItem(String ownerRestaurantId, String id, String name, BigDecimal price,
                        String category, int preparationMinutes, boolean available, int dailyStock) {
        super(ownerRestaurantId, requireText(id, "id"), requireText(name, "name"),
                requireText(category, "category"), preparationMinutes, available, dailyStock);
        this.price = requirePositivePrice(price);
    }

    @Override
    public ItemType type() {
        return ItemType.STANDARD;
    }

    @Override
    public Money unitPrice() {
        return price;
    }

    @Override
    public Money priceFor(BigDecimal quantity) {
        return price.times(quantity);
    }

    @Override
    public void validateQuantity(BigDecimal quantity) throws ValidationException {
        requireWholeQuantity(quantity);
    }

    @Override
    public String quantityUnit() {
        return "portion";
    }

    @Override
    public int unitsFor(BigDecimal quantity) {
        return quantity == null ? 0 : quantity.setScale(0, java.math.RoundingMode.FLOOR).intValueExact();
    }
}
