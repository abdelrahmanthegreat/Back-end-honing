package masr;

import java.time.LocalDate;
import java.util.List;

public final class FixedAmountPromotion extends Promotion {

    private final Money amount;

    public FixedAmountPromotion(String code, String description, Money amount, LocalDate expiresOn,
                                List<PromotionCondition> conditions) {
        super(code, description, expiresOn, conditions);
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("a fixed amount promotion needs an amount greater than zero");
        }
        this.amount = amount;
    }

    public static FixedAmountPromotion of(String code, Money amount, LocalDate expiresOn, PromotionCondition... conditions) {
        return new FixedAmountPromotion(code, amount.format() + " EGP off the order subtotal", amount, expiresOn,
                conditionsOf(conditions));
    }

    public Money amount() {
        return amount;
    }

    @Override
    public String kind() {
        return "fixed amount off the subtotal";
    }

    @Override
    public Money subtotalDiscount(PromotionContext context) {
        return amount.min(context.subtotal());
    }
}
