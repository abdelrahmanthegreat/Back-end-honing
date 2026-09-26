package masr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MenuItemFactory {

    private static final Map<ItemType, MenuItemCreator> REGISTRY = registerDefaults();

    private MenuItemFactory() {
    }

    private static Map<ItemType, MenuItemCreator> registerDefaults() {
        Map<ItemType, MenuItemCreator> registry = new EnumMap<>(ItemType.class);
        registry.put(ItemType.STANDARD, MenuItemCreator.ofStandard());
        registry.put(ItemType.COMBO, MenuItemCreator.ofCombo());
        registry.put(ItemType.WEIGHTED, MenuItemCreator.ofWeighted());
        return Map.copyOf(registry);
    }

    public static Map<ItemType, MenuItemCreator> registry() {
        return REGISTRY;
    }

    public static MenuItem create(MenuItemSpec spec, Restaurant menuOwner) throws ValidationException {
        if (spec == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "menu item specification is required");
        }
        if (menuOwner == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a menu owner restaurant is required");
        }
        ItemType type = spec.resolvedType();
        MenuItemCreator creator = REGISTRY.get(type);
        if (creator == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT,
                    "no creator registered for menu item type " + type);
        }
        validateCommon(spec);
        MenuItem created = creator.create(spec, menuOwner);
        if (menuOwner.findItem(created.id()).isPresent()) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    "menu item id '" + created.id() + "' already exists on the menu of " + menuOwner.name());
        }
        return created;
    }

    public static List<MenuItem> loadFrom(Reader source, Restaurant menuOwner) throws ValidationException, IOException {
        List<MenuItem> created = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                try {
                    created.add(create(MenuItemSpec.fromCsvLine(trimmed), menuOwner));
                } catch (PlatformException e) {
                    throw new ValidationException(ValidationException.INVALID_INPUT,
                            "line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return List.copyOf(created);
    }

    private static void validateCommon(MenuItemSpec spec) throws ValidationException {
        if (spec.id() == null || spec.id().isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a menu item id is required");
        }
        if (spec.name() == null || spec.name().isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a menu item name is required");
        }
        if (spec.price() == null || spec.price().signum() <= 0) {
            throw new ValidationException(ValidationException.INVALID_PRICE,
                    "the price of '" + spec.id() + "' must be greater than zero");
        }
        if (spec.category() == null || spec.category().isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT,
                    "a category is required for menu item '" + spec.id() + "'");
        }
        if (spec.preparationMinutes() <= 0) {
            throw new ValidationException(ValidationException.INVALID_VALUE,
                    "the preparation time of '" + spec.id() + "' must be greater than zero minutes");
        }
        if (spec.dailyStock() < 0) {
            throw new ValidationException(ValidationException.INVALID_VALUE,
                    "the daily stock of '" + spec.id() + "' must not be negative");
        }
    }
}
