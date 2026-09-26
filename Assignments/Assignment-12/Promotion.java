package masr;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class Promotion {

    private final String code;
    private final String description;
    private final LocalDate expiresOn;
    private final List<PromotionCondition> conditions;

    protected Promotion(String code, String description, LocalDate expiresOn, List<PromotionCondition> conditions) {
        this.code = normaliseCode(code);
        this.description = description == null ? "" : description.trim();
        this.expiresOn = expiresOn;
        this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }

    public static String normaliseCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("a promotion code is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public LocalDate expiresOn() {
        return expiresOn;
    }

    public List<PromotionCondition> conditions() {
        return conditions;
    }

    public abstract String kind();

    public abstract Money subtotalDiscount(PromotionContext context);

    public Money deliveryFeeReduction(Money deliveryFee, PromotionContext context) {
        return Money.ZERO;
    }

    public final void verify(PromotionContext context) throws PromotionException {
        Objects.requireNonNull(context, "context must not be null");
        if (expiresOn != null && context.orderDate() != null && context.orderDate().isAfter(expiresOn)) {
            throw new PromotionException(PromotionException.EXPIRED,
                    "promotion " + code + " expired on " + expiresOn);
        }
        for (PromotionCondition condition : conditions) {
            condition.check(context);
        }
    }

    public final Money discountFor(PromotionContext context) throws PromotionException {
        verify(context);
        Money discount = subtotalDiscount(context);
        return discount == null ? Money.ZERO : discount.max(Money.ZERO);
    }

    public final String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(code).append(" - ").append(kind());
        if (!description.isEmpty()) {
            sb.append(" (").append(description).append(')');
        }
        if (expiresOn != null) {
            sb.append(", expires ").append(expiresOn);
        }
        for (PromotionCondition condition : conditions) {
            sb.append(" + ").append(condition.name());
        }
        return sb.toString();
    }

    public static List<PromotionCondition> conditionsOf(PromotionCondition... conditions) {
        List<PromotionCondition> list = new ArrayList<>();
        if (conditions != null) {
            for (PromotionCondition condition : conditions) {
                if (condition != null) {
                    list.add(condition);
                }
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Promotion other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }
}
