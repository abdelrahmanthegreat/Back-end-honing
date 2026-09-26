package masr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record MenuItemSpec(String type,
                           String id,
                           String name,
                           BigDecimal price,
                           String category,
                           int preparationMinutes,
                           boolean available,
                           int dailyStock,
                           List<String> componentIds) {

    public MenuItemSpec {
        componentIds = componentIds == null ? List.of() : List.copyOf(componentIds);
    }

    public ItemType resolvedType() throws ValidationException {
        ItemType resolved = type == null ? null : ItemType.parse(type);
        if (resolved == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT,
                    "unknown menu item type '" + type + "' - expected one of " + List.of(ItemType.values()));
        }
        return resolved;
    }

    public static MenuItemSpec fromMap(Map<String, String> row) {
        return new MenuItemSpec(
                row.get("type"),
                row.get("id"),
                row.get("name"),
                parseDecimal(row.get("price")),
                row.get("category"),
                parseInt(row.get("preparationMinutes"), 20),
                parseBoolean(row.get("available"), true),
                parseInt(row.get("dailyStock"), 50),
                splitComponents(row.get("components")));
    }

    public static MenuItemSpec fromCsvLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] cells = line.split("\\|", -1);
        return new MenuItemSpec(
                cell(cells, 0),
                cell(cells, 1),
                cell(cells, 2),
                parseDecimal(cell(cells, 3)),
                cell(cells, 4),
                parseInt(cell(cells, 5), 20),
                parseBoolean(cell(cells, 6), true),
                parseInt(cell(cells, 7), 50),
                splitComponents(cell(cells, 8)));
    }

    private static String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : null;
    }

    private static List<String> splitComponents(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(";")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static BigDecimal parseDecimal(String raw) {
        return raw == null || raw.isBlank() ? null : new BigDecimal(raw.trim());
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return raw == null || raw.isBlank() ? fallback : Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "true", "yes", "y", "1", "available" -> true;
            case "false", "no", "n", "0", "unavailable" -> false;
            default -> fallback;
        };
    }

    public String describe() {
        return (id == null ? "?" : id) + " " + (name == null ? "" : name) + " [" + type + "] "
                + (price == null ? "?" : price.toPlainString()) + " EGP";
    }
}
