package masr;

import java.util.Locale;

public enum Cuisine {

    EGYPTIAN("Egyptian"),
    STREET_FOOD("Street Food"),
    ITALIAN("Italian"),
    PIZZA("Pizza"),
    FAST_FOOD("Fast Food"),
    BURGERS("Burgers"),
    JAPANESE("Japanese"),
    SUSHI("Sushi"),
    CHINESE("Chinese"),
    INDIAN("Indian"),
    LEBANESE("Lebanese"),
    SYRIAN("Syrian"),
    SEAFOOD("Seafood"),
    GRILL("Grill"),
    KOREAN("Korean"),
    MEXICAN("Mexican"),
    VEGETARIAN("Vegetarian"),
    BREAKFAST("Breakfast"),
    DESSERTS("Desserts"),
    COFFEE("Coffee");

    private final String label;

    Cuisine(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean matches(String freeText) {
        if (freeText == null || freeText.isBlank()) {
            return false;
        }
        String needle = freeText.trim().toLowerCase(Locale.ROOT);
        return label.toLowerCase(Locale.ROOT).contains(needle) || name().toLowerCase(Locale.ROOT).replace('_', ' ').contains(needle);
    }

    public static Cuisine parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String candidate = text.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (Cuisine cuisine : values()) {
            if (cuisine.name().equals(candidate) || cuisine.label.toUpperCase(Locale.ROOT).replace(' ', '_').equals(candidate)) {
                return cuisine;
            }
        }
        return null;
    }

    public static String options() {
        StringBuilder sb = new StringBuilder();
        for (Cuisine cuisine : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(cuisine.label());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return label;
    }
}
