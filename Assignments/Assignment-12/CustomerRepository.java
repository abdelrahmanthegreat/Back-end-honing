package masr;

import java.util.List;
import java.util.Optional;

public final class CustomerRepository extends Repository<Customer> {

    public CustomerRepository() {
        super("customer", Customer::id);
    }

    public void notifyCustomer(String customerId, String message) {
        find(customerId).ifPresent(customer -> customer.notify(message));
    }

    public Optional<Customer> findByMobile(String mobile) {
        if (mobile == null) {
            return Optional.empty();
        }
        String candidate = mobile.trim();
        return stream().filter(customer -> customer.mobile().equals(candidate)).findFirst();
    }

    public List<Customer> byTier(LoyaltyTier tier) {
        return findAll(customer -> customer.tier() == tier);
    }
}
