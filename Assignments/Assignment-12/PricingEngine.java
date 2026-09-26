package masr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

public final class PricingEngine {

    private final PlatformConfig config;
    private final GeoService geo;

    public PricingEngine(GeoService geo) {
        this(PlatformConfig.get(), geo);
    }

    public PricingEngine(PlatformConfig config, GeoService geo) {
        this.config = config;
        this.geo = geo;
    }

    public PriceBreakdown price(Customer customer, Restaurant restaurant, Address address,
                                Collection<OrderLine> lines, Promotion promotion, LocalDate orderDate)
            throws PromotionException {
        if (customer == null || restaurant == null || address == null) {
            throw new IllegalArgumentException("customer, restaurant and address are required to price an order");
        }
        Money subtotal = subtotal(lines);
        int distanceKm = geo.distanceKm(restaurant.district(), address.district());

        Money rawDeliveryFee = deliveryFee(distanceKm);
        LoyaltyTier tier = customer.tier();
        Money loyaltyDeliveryFee = tier.applyToDeliveryFee(rawDeliveryFee);

        Money feeReduction = Money.ZERO;
        Money promotionDiscount = Money.ZERO;
        String promotionCode = null;
        if (promotion != null) {
            PromotionContext context = new PromotionContext(subtotal, restaurant.district(), address.district(), customer, orderDate);
            promotion.verify(context);
            feeReduction = promotion.deliveryFeeReduction(loyaltyDeliveryFee, context).max(Money.ZERO);
            promotionDiscount = promotion.discountFor(context);
            promotionCode = promotion.code();
        }

        Money deliveryFee = loyaltyDeliveryFee.minus(feeReduction).max(Money.ZERO);
        Money serviceFee = subtotal.timesRate(config.serviceFeeRate());
        Money total = subtotal.plus(deliveryFee).plus(serviceFee).minus(promotionDiscount).max(Money.ZERO);

        return new PriceBreakdown(subtotal, rawDeliveryFee, loyaltyDeliveryFee, feeReduction,
                deliveryFee, serviceFee, promotionDiscount, total, promotionCode, distanceKm, tier);
    }

    public Money subtotal(Collection<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return Money.ZERO;
        }
        return lines.stream().map(OrderLine::lineTotal).reduce(Money.ZERO, Money::plus);
    }

    public Money deliveryFee(int distanceKm) {
        long billableExtraKm = Math.max(0L, distanceKm - config.includedKilometres());
        return config.baseDeliveryFee().plus(config.perExtraKmFee().times(billableExtraKm));
    }

    public BigDecimal serviceFeeRate() {
        return config.serviceFeeRate();
    }
}
