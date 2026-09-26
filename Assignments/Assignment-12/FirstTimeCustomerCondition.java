package masr;

public final class FirstTimeCustomerCondition implements PromotionCondition {

    @Override
    public void check(PromotionContext context) throws PromotionException {
        if (!context.isFirstTimeCustomer()) {
            throw new PromotionException(PromotionException.NOT_FIRST_TIME,
                    "this promotion is only valid for a customer's first ever order");
        }
    }

    @Override
    public String name() {
        return "first time customers only";
    }

    @Override
    public String toString() {
        return name();
    }
}
