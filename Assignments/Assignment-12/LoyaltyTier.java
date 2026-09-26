package masr;

import java.math.BigDecimal;

public enum LoyaltyTier {

    BRONZE("Bronze", 0, BigDecimal.ZERO, false, 1),
    SILVER("Silver", 10, new BigDecimal("0.10"), false, 1),
    GOLD("Gold", 30, BigDecimal.ONE, true, 0);

    private final String label;
    private final int minimumCompletedOrders;
    private final BigDecimal deliveryFeeRate;
    private final boolean waivesDeliveryFee;
    private final int dispatchPriority;

    LoyaltyTier(String label, int minimumCompletedOrders, BigDecimal deliveryFeeRate, boolean waivesDeliveryFee, int dispatchPriority) {
        this.label = label;
        this.minimumCompletedOrders = minimumCompletedOrders;
        this.deliveryFeeRate = deliveryFeeRate;
        this.waivesDeliveryFee = waivesDeliveryFee;
        this.dispatchPriority = dispatchPriority;
    }

    public String label() {
        return label;
    }

    public int minimumCompletedOrders() {
        return minimumCompletedOrders;
    }

    public BigDecimal deliveryFeeRate() {
        return deliveryFeeRate;
    }

    public boolean waivesDeliveryFee() {
        return waivesDeliveryFee;
    }

    public int dispatchPriority() {
        return dispatchPriority;
    }

    public static LoyaltyTier fromCompletedOrders(int completedOrderCount) {
        LoyaltyTier result = BRONZE;
        for (LoyaltyTier tier : values()) {
            if (completedOrderCount >= tier.minimumCompletedOrders) {
                result = tier;
            }
        }
        return result;
    }

    public Money applyToDeliveryFee(Money deliveryFee) {
        if (deliveryFee == null) {
            return Money.ZERO;
        }
        if (waivesDeliveryFee) {
            return Money.ZERO;
        }
        Money discounted = deliveryFee.times(BigDecimal.ONE.subtract(deliveryFeeRate));
        return discounted.atLeast(Money.ZERO);
    }

    @Override
    public String toString() {
        return label;
    }
}
