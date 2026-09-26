package masr;

import java.time.LocalDate;
import java.util.List;

public final class FreeDeliveryPromotion extends Promotion {

    public FreeDeliveryPromotion(String code, String description, LocalDate expiresOn, List<PromotionCondition> conditions) {
        super(code, description, expiresOn, conditions);
    }

    public static FreeDeliveryPromotion of(String code, LocalDate expiresOn, PromotionCondition... conditions) {
        return new FreeDeliveryPromotion(code, "delivery fee waived", expiresOn, conditionsOf(conditions));
    }

    @Override
    public String kind() {
        return "free delivery";
    }

    @Override
    public Money subtotalDiscount(PromotionContext context) {
        return Money.ZERO;
    }

    @Override
    public Money deliveryFeeReduction(Money deliveryFee, PromotionContext context) {
        return deliveryFee == null ? Money.ZERO : deliveryFee.max(Money.ZERO);
    }
}
