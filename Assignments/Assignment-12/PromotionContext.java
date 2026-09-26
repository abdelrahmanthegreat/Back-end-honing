package masr;

import java.time.LocalDate;

public record PromotionContext(Money subtotal,
                               District restaurantDistrict,
                               District deliveryDistrict,
                               Customer customer,
                               LocalDate orderDate) {

    public boolean isFirstTimeCustomer() {
        return customer != null && customer.isFirstTimeCustomer();
    }

    public String describe() {
        return "subtotal " + subtotal + ", delivering to " + (deliveryDistrict == null ? "?" : deliveryDistrict.label())
                + ", customer " + (customer == null ? "?" : customer.id());
    }
}
