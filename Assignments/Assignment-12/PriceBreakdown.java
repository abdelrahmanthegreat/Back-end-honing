package masr;

import java.math.BigDecimal;
import java.util.Locale;

public record PriceBreakdown(Money subtotal,
                             Money rawDeliveryFee,
                             Money loyaltyDeliveryFee,
                             Money promotionFeeReduction,
                             Money deliveryFee,
                             Money serviceFee,
                             Money promotionDiscount,
                             Money total,
                             String promotionCode,
                             int distanceKm,
                             LoyaltyTier loyaltyTier) {

    public Money fees() {
        return deliveryFee.plus(serviceFee);
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Subtotal        : ").append(format(subtotal)).append('\n');
        sb.append("Delivery fee    : ").append(format(deliveryFee))
                .append("   (").append(distanceKm).append(" km, ")
                .append(loyaltyTier.waivesDeliveryFee() ? "Gold waives the fee"
                        : loyaltyTier.deliveryFeeRate().signum() == 0 ? "Bronze pays full price"
                        : loyaltyTier.label() + " gets " + loyaltyTier.deliveryFeeRate().multiply(new java.math.BigDecimal("100")).toPlainString() + "% off")
                .append(promotionFeeReduction.isPositive() ? ", promotion waived " + format(promotionFeeReduction) : "")
                .append(")\n");
        sb.append("Service fee     : ").append(format(serviceFee)).append('\n');
        sb.append("Promotion       : ").append(promotionCode == null ? "none" : promotionCode + " -" + format(promotionDiscount)).append('\n');
        sb.append("Total           : ").append(format(total)).append('\n');
        return sb.toString();
    }

    public String oneLine() {
        return "subtotal " + format(subtotal)
                + " + delivery " + format(deliveryFee)
                + " + service " + format(serviceFee)
                + " - promotion " + format(promotionDiscount)
                + " = " + format(total) + " EGP";
    }

    public String tableRow() {
        return String.format(Locale.ROOT, "%10s %10s %10s %10s %12s",
                format(subtotal), format(deliveryFee), format(serviceFee), format(promotionDiscount), format(total));
    }

    public static String tableHeader() {
        return String.format(Locale.ROOT, "%10s %10s %10s %10s %12s", "SUBTOTAL", "DELIVERY", "SERVICE", "PROMO", "TOTAL");
    }

    private static String format(Money money) {
        return money == null ? Money.ZERO.format() : money.format();
    }
}
