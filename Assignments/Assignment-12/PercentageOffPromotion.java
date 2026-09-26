package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PercentageOffPromotion extends Promotion {

    private final BigDecimal rate;
    private final Money cap;

    public PercentageOffPromotion(String code, String description, BigDecimal percent, Money cap,
                                  LocalDate expiresOn, List<PromotionCondition> conditions) {
        super(code, description, expiresOn, conditions);
        if (percent == null || percent.signum() <= 0) {
            throw new IllegalArgumentException("a percentage promotion needs a percentage greater than zero");
        }
        if (percent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("a percentage promotion cannot exceed 100%");
        }
        this.rate = percent.movePointLeft(2);
        this.cap = cap;
    }

    public static PercentageOffPromotion of(String code, BigDecimal percent, Money cap,
                                            LocalDate expiresOn, PromotionCondition... conditions) {
        return new PercentageOffPromotion(code, percent + "% off the order subtotal", percent, cap, expiresOn,
                conditionsOf(conditions));
    }

    public BigDecimal percent() {
        return rate.movePointRight(2);
    }

    public Money cap() {
        return cap;
    }

    @Override
    public String kind() {
        return "percentage off the subtotal" + (cap == null ? "" : " capped at " + cap);
    }

    @Override
    public Money subtotalDiscount(PromotionContext context) {
        Money raw = context.subtotal().timesRate(rate);
        return cap == null ? raw : raw.min(cap);
    }
}
