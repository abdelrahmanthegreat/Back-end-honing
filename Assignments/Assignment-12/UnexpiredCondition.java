package masr;

import java.time.LocalDate;

public record UnexpiredCondition(LocalDate today) implements PromotionCondition {

    @Override
    public void check(PromotionContext context) throws PromotionException {
        if (today != null && context.orderDate() != null && context.orderDate().isAfter(today)) {
            throw new PromotionException(PromotionException.EXPIRED, "this promotion expired on " + today);
        }
    }

    @Override
    public String name() {
        return "unexpired on " + today;
    }
}
