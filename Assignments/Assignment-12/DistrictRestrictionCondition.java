package masr;

public record DistrictRestrictionCondition(District allowed) implements PromotionCondition {

    @Override
    public void check(PromotionContext context) throws PromotionException {
        if (allowed != null && context.deliveryDistrict() != null && context.deliveryDistrict() != allowed) {
            throw new PromotionException(PromotionException.DISTRICT_NOT_ALLOWED,
                    "this promotion is limited to " + allowed.label() + " deliveries, this order delivers to "
                            + context.deliveryDistrict().label());
        }
    }

    @Override
    public String name() {
        return "district " + (allowed == null ? "any" : allowed.label());
    }
}
