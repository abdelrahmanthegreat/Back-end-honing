package masr;

import java.util.Locale;

public enum District {

    MAADI("Maadi"),
    DOKKI("Dokki"),
    NASR_CITY("Nasr City"),
    HELIOPOLIS("Heliopolis"),
    FAISAL("Faisal"),
    ZAMALEK("Zamalek"),
    GIZA("Giza"),
    AIN_SHAMS("Ain Shams");

    private final String label;

    District(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static District parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String candidate = text.trim().toUpperCase(Locale.ROOT);
        for (District district : values()) {
            if (district.name().equals(candidate) || district.label.toUpperCase(Locale.ROOT).equals(candidate)) {
                return district;
            }
        }
        return null;
    }

    public static String options() {
        StringBuilder sb = new StringBuilder();
        for (District district : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(district.label());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return label;
    }
}
