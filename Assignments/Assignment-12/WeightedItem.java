package masr;

import java.math.BigDecimal;

public final class WeightedItem extends MenuItem {

    private static final BigDecimal MIN_WEIGHT_KG = new BigDecimal("0.05");

    private final Money pricePerKg;

    public WeightedItem(String ownerRestaurantId, String id, String name, BigDecimal pricePerKg,
                        String category, int preparationMinutes, boolean available, int dailyStock) {
        super(ownerRestaurantId, requireText(id, "id"), requireText(name, "name"),
                requireText(category, "category"), preparationMinutes, available, dailyStock);
        this.pricePerKg = requirePositivePrice(pricePerKg);
    }

    public Money pricePerKg() {
        return pricePerKg;
    }

    @Override
    public ItemType type() {
        return ItemType.WEIGHTED;
    }

    @Override
    public Money unitPrice() {
        return pricePerKg;
    }

    @Override
    public Money priceFor(BigDecimal quantity) {
        return pricePerKg.times(quantity);
    }

    @Override
    public void validateQuantity(BigDecimal quantity) throws ValidationException {
        requirePositiveQuantity(quantity);
        if (quantity.compareTo(MIN_WEIGHT_KG) < 0) {
            throw new ValidationException(ValidationException.INVALID_QUANTITY,
                    "'" + name() + "' is sold by weight, the minimum order is " + MIN_WEIGHT_KG.toPlainString() + " kg");
        }
    }

    @Override
    public String quantityUnit() {
        return "kg";
    }

    @Override
    public int unitsFor(BigDecimal quantity) {
        if (quantity == null) {
            return 0;
        }
        return quantity.multiply(new BigDecimal(1000)).setScale(0, java.math.RoundingMode.HALF_UP).intValueExact() / 1000;
    }

    @Override
    public String describePrice() {
        return pricePerKg.format() + " EGP per kg";
    }
}
