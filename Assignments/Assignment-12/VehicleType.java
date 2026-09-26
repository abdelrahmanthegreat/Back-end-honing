package masr;

import java.util.Locale;

public enum VehicleType {

    MOTORCYCLE("Motorcycle"),
    BICYCLE("Bicycle"),
    CAR("Car");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static VehicleType parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String candidate = text.trim().toUpperCase(Locale.ROOT);
        for (VehicleType type : values()) {
            if (type.name().equals(candidate) || type.label.toUpperCase(Locale.ROOT).equals(candidate)) {
                return type;
            }
        }
        return null;
    }

    public static String options() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(i + 1).append(") ").append(values()[i].label());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return label;
    }
}
