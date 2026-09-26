package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PromotionBuilderRegistry {

    public static final String PERCENT_OFF = "PERCENT_OFF";
    public static final String FIXED_OFF = "FIXED_OFF";
    public static final String FREE_DELIVERY = "FREE_DELIVERY";

    private static final Map<String, PromotionBuilder> BUILDERS =
            Collections.synchronizedMap(new LinkedHashMap<>(registerDefaults()));

    private PromotionBuilderRegistry() {
    }

    private static Map<String, PromotionBuilder> registerDefaults() {
        Map<String, PromotionBuilder> builders = new LinkedHashMap<>();
        builders.put(PERCENT_OFF, PromotionBuilderRegistry::percentOff);
        builders.put(FIXED_OFF, PromotionBuilderRegistry::fixedOff);
        builders.put(FREE_DELIVERY, PromotionBuilderRegistry::freeDelivery);
        return builders;
    }

    public static void register(String kind, PromotionBuilder builder) {
        BUILDERS.put(kind, builder);
    }

    public static List<String> kinds() {
        synchronized (BUILDERS) {
            return List.copyOf(BUILDERS.keySet());
        }
    }

    public static Promotion build(String kind, ConsoleIo io, String code) throws PlatformException {
        PromotionBuilder builder;
        synchronized (BUILDERS) {
            builder = BUILDERS.get(kind);
        }
        if (builder == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT,
                    "no promotion builder registered for '" + kind + "' - known kinds: " + kinds());
        }
        return builder.build(io, code);
    }

    private static Promotion percentOff(ConsoleIo io, String code) throws PlatformException {
        BigDecimal percent = io.readDecimal("Percentage off the subtotal (1-100): ");
        if (percent.signum() <= 0 || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException(ValidationException.INVALID_VALUE,
                    "a percentage promotion must be between 1 and 100");
        }
        Money cap = null;
        String capText = io.readLine("Maximum discount in EGP (blank for no cap): ");
        if (!capText.isBlank()) {
            cap = Money.ofEgp(new BigDecimal(capText));
            if (!cap.isPositive()) {
                throw new ValidationException(ValidationException.INVALID_PRICE, "the cap must be greater than zero");
            }
        }
        LocalDate expiresOn = askExpiry(io);
        List<PromotionCondition> conditions = askConditions(io);
        return PercentageOffPromotion.of(code, percent, cap, expiresOn, conditions.toArray(new PromotionCondition[0]));
    }

    private static Promotion fixedOff(ConsoleIo io, String code) throws PlatformException {
        Money amount = Money.ofEgp(io.readPositiveDecimal("Amount off the subtotal in EGP: "));
        LocalDate expiresOn = askExpiry(io);
        List<PromotionCondition> conditions = askConditions(io);
        return FixedAmountPromotion.of(code, amount, expiresOn, conditions.toArray(new PromotionCondition[0]));
    }

    private static Promotion freeDelivery(ConsoleIo io, String code) throws PlatformException {
        LocalDate expiresOn = askExpiry(io);
        List<PromotionCondition> conditions = askConditions(io);
        return FreeDeliveryPromotion.of(code, expiresOn, conditions.toArray(new PromotionCondition[0]));
    }

    private static LocalDate askExpiry(ConsoleIo io) {
        while (true) {
            String text = io.readLine("Expiry date yyyy-mm-dd (blank for no expiry): ");
            if (text.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(text);
            } catch (java.time.format.DateTimeParseException e) {
                io.warn("'" + text + "' is not a date, use yyyy-mm-dd for example 2026-12-31.");
            }
        }
    }

    private static List<PromotionCondition> askConditions(ConsoleIo io) throws PlatformException {
        java.util.List<PromotionCondition> conditions = new java.util.ArrayList<>();
        while (true) {
            io.println("conditions: 1. minimum subtotal  2. district limit  3. first time customers only  0. done");
            int choice = io.readChoice("Add a condition? ", 4);
            if (choice == 0) {
                return List.copyOf(conditions);
            }
            if (choice == 1) {
                conditions.add(PromotionCondition.minimumSubtotal(
                        Money.ofEgp(io.readPositiveDecimal("Minimum subtotal in EGP: "))));
            } else if (choice == 2) {
                io.println("districts: " + District.options());
                District district = District.parse(io.readRequired("Allowed delivery district: "));
                if (district == null) {
                    throw new ValidationException(ValidationException.INVALID_INPUT, "unknown district");
                }
                conditions.add(PromotionCondition.districtAllowed(district));
            } else {
                conditions.add(PromotionCondition.firstTimeCustomersOnly());
            }
        }
    }

    @FunctionalInterface
    public interface PromotionBuilder {
        Promotion build(ConsoleIo io, String code) throws PlatformException;
    }
}
