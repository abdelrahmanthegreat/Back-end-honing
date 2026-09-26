package masr;

import java.util.Locale;

public enum ItemType {

    STANDARD("Standard item - sold at its listed price"),
    COMBO("Combo - several items bundled at a discount"),
    WEIGHTED("Weighted item - priced per kilogram");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ItemType parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String candidate = text.trim().toUpperCase(Locale.ROOT);
        for (ItemType type : values()) {
            if (type.name().equals(candidate)) {
                return type;
            }
        }
        return null;
    }

    public static String options() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values().length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(' ').append(i + 1).append(". ").append(values()[i].label());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return name();
    }
}
