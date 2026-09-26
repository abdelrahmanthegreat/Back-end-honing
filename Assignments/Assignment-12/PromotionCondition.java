package masr;

import java.time.LocalDate;

public interface PromotionCondition {

    void check(PromotionContext context) throws PromotionException;

    String name();

    static PromotionCondition minimumSubtotal(Money minimum) {
        return new MinimumSubtotalCondition(minimum);
    }

    static PromotionCondition districtAllowed(District allowed) {
        return new DistrictRestrictionCondition(allowed);
    }

    static PromotionCondition firstTimeCustomersOnly() {
        return new FirstTimeCustomerCondition();
    }

    static PromotionCondition unexpiredOn(LocalDate today) {
        return new UnexpiredCondition(today);
    }
}
