package masr;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ComboItem extends MenuItem {

    private final List<MenuItem> components;
    private final Money bundlePrice;

    public ComboItem(String ownerRestaurantId, String id, String name, BigDecimal bundlePrice,
                     List<MenuItem> components, String category, int preparationMinutes,
                     boolean available, int dailyStock) {
        super(ownerRestaurantId, requireText(id, "id"), requireText(name, "name"),
                requireText(category, "category"), preparationMinutes, available, dailyStock);
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("a combo must contain at least one component");
        }
        Set<MenuItem> distinct = new LinkedHashSet<>(components);
        if (distinct.size() != components.size()) {
            throw new IllegalArgumentException("a combo must not repeat the same component");
        }
        this.components = List.copyOf(distinct);
        Money bundle = requirePositivePrice(bundlePrice);
        Money partsTotal = components.stream().map(MenuItem::unitPrice).reduce(Money.ZERO, Money::plus);
        if (bundle.compareTo(partsTotal) >= 0) {
            throw new IllegalArgumentException("a combo must be cheaper than its parts, parts total "
                    + partsTotal + " but the bundle is " + bundle);
        }
        this.bundlePrice = bundle;
    }

    public List<MenuItem> components() {
        return components;
    }

    public Money componentsTotal() {
        return components.stream().map(MenuItem::unitPrice).reduce(Money.ZERO, Money::plus);
    }

    public Money savings() {
        return componentsTotal().minus(bundlePrice).max(Money.ZERO);
    }

    @Override
    public ItemType type() {
        return ItemType.COMBO;
    }

    @Override
    public Money unitPrice() {
        return bundlePrice;
    }

    @Override
    public Money priceFor(BigDecimal quantity) {
        return bundlePrice.times(quantity);
    }

    @Override
    public void validateQuantity(BigDecimal quantity) throws ValidationException {
        requireWholeQuantity(quantity);
    }

    @Override
    public String quantityUnit() {
        return "combo";
    }

    @Override
    public int unitsFor(BigDecimal quantity) {
        return quantity == null ? 0 : quantity.setScale(0, java.math.RoundingMode.FLOOR).intValueExact();
    }

    @Override
    public String describePrice() {
        return bundlePrice.format() + " EGP per combo (saves " + savings().format() + " EGP)";
    }
}
