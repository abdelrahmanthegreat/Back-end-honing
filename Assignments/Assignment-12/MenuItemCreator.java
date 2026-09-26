package masr;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface MenuItemCreator {

    MenuItem create(MenuItemSpec spec, Restaurant menuOwner) throws ValidationException;

    static MenuItemCreator ofStandard() {
        return (spec, owner) -> new StandardItem(owner.id(), spec.id(), spec.name(), spec.price(),
                spec.category(), spec.preparationMinutes(), spec.available(), spec.dailyStock());
    }

    static MenuItemCreator ofCombo() {
        return (spec, owner) -> {
            List<MenuItem> components = new ArrayList<>();
            for (String componentId : spec.componentIds()) {
                MenuItem component = owner.findItem(componentId).orElseThrow(() -> new ValidationException(
                        ValidationException.INVALID_INPUT,
                        "combo '" + spec.id() + "' references unknown component '" + componentId + "'"));
                components.add(component);
            }
            return new ComboItem(owner.id(), spec.id(), spec.name(), spec.price(), components,
                    spec.category(), spec.preparationMinutes(), spec.available(), spec.dailyStock());
        };
    }

    static MenuItemCreator ofWeighted() {
        return (spec, owner) -> new WeightedItem(owner.id(), spec.id(), spec.name(), spec.price(),
                spec.category(), spec.preparationMinutes(), spec.available(), spec.dailyStock());
    }
}
