package masr;

public record MinimumSubtotalCondition(Money minimum) implements PromotionCondition {

    @Override
    public void check(PromotionContext context) throws PromotionException {
        if (minimum != null && context.subtotal().isPositive() && context.subtotal().compareTo(minimum) < 0) {
            throw new PromotionException(PromotionException.MINIMUM_SUBTOTAL_NOT_MET,
                    "this promotion needs a subtotal of at least " + minimum
                            + " but the order subtotal is " + context.subtotal());
        }
    }

    @Override
    public String name() {
        return "minimum subtotal " + minimum;
    }
}
