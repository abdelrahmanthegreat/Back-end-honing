package masr;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class Restaurant {

    private final String id;
    private String name;
    private final District district;
    private final Set<Cuisine> cuisines;
    private double rating;
    private double ratingCount;
    private boolean open;
    private final Map<String, MenuItem> menu;

    public Restaurant(String id, String name, District district, Collection<Cuisine> cuisines,
                      double rating, boolean open) {
        this.id = requireText(id, "restaurant id");
        this.name = requireText(name, "restaurant name");
        this.district = Objects.requireNonNull(district, "district must not be null");
        if (cuisines == null || cuisines.isEmpty()) {
            throw new IllegalArgumentException("a restaurant must declare at least one cuisine");
        }
        this.cuisines = EnumSet.copyOf(cuisines);
        this.rating = requireRating(rating);
        this.ratingCount = 1.0d;
        this.open = open;
        this.menu = new LinkedHashMap<>();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public District district() {
        return district;
    }

    public Set<Cuisine> cuisines() {
        return Set.copyOf(cuisines);
    }

    public double rating() {
        return rating;
    }

    public long sampleCount() {
        return (long) ratingCount;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void addCuisine(Cuisine cuisine) {
        cuisines.add(Objects.requireNonNull(cuisine, "cuisine must not be null"));
    }

    public void rate(double stars) {
        double bounded = requireRating(stars);
        double newCount = ratingCount + 1.0d;
        this.rating = round((this.rating * ratingCount + bounded) / newCount);
        this.ratingCount = newCount;
    }

    public void setRating(double stars) {
        this.rating = requireRating(stars);
        this.ratingCount = Math.max(1.0d, this.ratingCount);
    }

    public void addMenuItem(MenuItem item) throws ValidationException {
        Objects.requireNonNull(item, "item must not be null");
        if (!item.ownerRestaurantId().equals(id)) {
            throw new ValidationException(ValidationException.INVALID_INPUT,
                    "menu item '" + item.id() + "' belongs to another restaurant");
        }
        if (menu.containsKey(item.id())) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    "menu item id '" + item.id() + "' already exists on this menu");
        }
        menu.put(item.id(), item);
    }

    public Optional<MenuItem> findItem(String itemId) {
        return itemId == null ? Optional.empty() : Optional.ofNullable(menu.get(itemId.trim()));
    }

    public MenuItem requireItem(String itemId) throws CatalogException {
        return findItem(itemId).orElseThrow(() -> new CatalogException(CatalogException.NO_SUCH_ITEM,
                "restaurant '" + name + "' has no menu item with id '" + itemId + "'"));
    }

    public List<MenuItem> menuItems() {
        return List.copyOf(menu.values());
    }

    public Stream<MenuItem> menuStream() {
        return List.copyOf(menu.values()).stream();
    }

    public List<MenuItem> availableItems() {
        return menu.values().stream().filter(MenuItem::isAvailable).toList();
    }

    public List<MenuItem> itemsInCategory(String category) {
        return menu.values().stream().filter(item -> item.category().equalsIgnoreCase(category)).toList();
    }

    public List<String> categories() {
        return menu.values().stream().map(MenuItem::category).distinct().sorted().toList();
    }

    public boolean removeItem(String itemId) {
        return itemId != null && menu.remove(itemId.trim()) != null;
    }

    public void setItemAvailability(String itemId, boolean available) throws CatalogException {
        requireItem(itemId).setAvailable(available);
    }

    public void setDailyStock(String itemId, int stock) throws StockException, CatalogException {
        if (stock < 0) {
            throw new StockException(StockException.INVALID_STOCK, "daily stock must not be negative");
        }
        requireItem(itemId).setDailyStock(stock);
    }

    public void startNewDay() {
        menu.values().forEach(MenuItem::startNewDay);
    }

    public Money lowestPrice() {
        return menu.values().stream().map(MenuItem::unitPrice).min(Money::compareTo).orElse(Money.ZERO);
    }

    public Money highestPrice() {
        return menu.values().stream().map(MenuItem::unitPrice).max(Money::compareTo).orElse(Money.ZERO);
    }

    public int menuSize() {
        return menu.size();
    }

    public String cuisineLine() {
        List<String> labels = cuisines.stream().map(Cuisine::label).sorted().toList();
        return String.join(", ", labels);
    }

    public String describe() {
        return name + " (" + id + ") - " + district.label() + " | " + cuisineLine()
                + " | rated " + String.format("%.1f", rating) + "/5 | " + (open ? "OPEN" : "CLOSED");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Restaurant other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    private static double requireRating(double stars) {
        if (Double.isNaN(stars) || stars < 0.0d || stars > 5.0d) {
            throw new IllegalArgumentException("rating must be between 0.0 and 5.0 inclusive");
        }
        return stars;
    }

    private static double round(double value) {
        return Math.round(value * 10.0d) / 10.0d;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
