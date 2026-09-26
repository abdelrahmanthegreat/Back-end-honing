package masr;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class Customer {

    public static final Pattern MOBILE = Pattern.compile("^01[0125]\\d{8}$");

    private final String id;
    private String name;
    private final String mobile;
    private final Set<Address> addresses;
    private final RecentSearchLog recentSearches;
    private final List<String> notifications;
    private Money balance;
    private int ordersPlaced;
    private int completedOrderCount;
    private int cancelledOrderCount;

    public Customer(String id, String name, String mobile, Money openingBalance) {
        this.id = requireText(id, "customer id");
        this.name = requireText(name, "customer name");
        this.mobile = normaliseMobile(mobile);
        if (openingBalance != null && openingBalance.isNegative()) {
            throw new IllegalArgumentException("a wallet balance must not be negative");
        }
        this.balance = openingBalance == null ? Money.ZERO : openingBalance;
        this.addresses = new LinkedHashSet<>();
        this.recentSearches = new RecentSearchLog(PlatformConfig.get().recentSearchCapacity());
        this.notifications = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String mobile() {
        return mobile;
    }

    public Money balance() {
        return balance;
    }

    public LoyaltyTier tier() {
        return LoyaltyTier.fromCompletedOrders(completedOrderCount);
    }

    public int completedOrderCount() {
        return completedOrderCount;
    }

    public int ordersPlaced() {
        return ordersPlaced;
    }

    public int cancelledOrderCount() {
        return cancelledOrderCount;
    }

    public boolean isFirstTimeCustomer() {
        return ordersPlaced == 0;
    }

    public Address addAddress(District district, String detail) throws ValidationException {
        Address address = Address.of(id, district, detail);
        if (!addresses.add(address)) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    "this exact address is already saved for " + name);
        }
        return address;
    }

    public void addAddress(Address address) throws ValidationException {
        if (!address.customerId().equals(id)) {
            throw new ValidationException(ValidationException.ADDRESS_NOT_OWNED,
                    "address belongs to customer " + address.customerId() + ", not " + id);
        }
        if (!addresses.add(address)) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    "this exact address is already saved for " + name);
        }
    }

    public boolean removeAddress(Address address) {
        return address != null && addresses.remove(address);
    }

    public Set<Address> addresses() {
        return Set.copyOf(addresses);
    }

    public List<Address> addressList() {
        return List.copyOf(addresses);
    }

    public boolean owns(Address address) {
        return address != null && addresses.contains(address);
    }

    public Optional<Address> findAddress(String detailFragment) {
        if (detailFragment == null || detailFragment.isBlank()) {
            return Optional.empty();
        }
        String needle = detailFragment.trim().toLowerCase(java.util.Locale.ROOT);
        return addressList().stream()
                .filter(address -> address.detail().toLowerCase(java.util.Locale.ROOT).contains(needle)
                        || address.district().label().toLowerCase(java.util.Locale.ROOT).contains(needle))
                .findFirst();
    }

    public void recordSearch(String term) {
        recentSearches.record(term);
    }

    public List<String> recentSearches() {
        return recentSearches.newestFirst();
    }

    public RecentSearchLog searchLog() {
        return recentSearches;
    }

    public synchronized void notify(String message) {
        notifications.add(message);
        int capacity = PlatformConfig.get().notificationHistoryCapacity();
        while (notifications.size() > capacity) {
            notifications.remove(0);
        }
    }

    public synchronized List<String> notifications() {
        return List.copyOf(notifications);
    }

    public void topUp(Money amount) throws PaymentException {
        if (amount == null || !amount.isPositive()) {
            throw new PaymentException(PaymentException.INVALID_AMOUNT, "a top up must be greater than zero");
        }
        balance = balance.plus(amount);
    }

    public void debit(Money amount) throws PaymentException {
        if (amount == null || !amount.isPositive()) {
            throw new PaymentException(PaymentException.INVALID_AMOUNT, "a payment must be greater than zero");
        }
        if (amount.compareTo(balance) > 0) {
            throw new PaymentException(PaymentException.INSUFFICIENT_FUNDS,
                    "wallet holds " + balance + " but the order costs " + amount);
        }
        balance = balance.minus(amount);
    }

    public void credit(Money amount) {
        if (amount != null && amount.isPositive()) {
            balance = balance.plus(amount);
        }
    }

    void markOrderPlaced() {
        ordersPlaced++;
    }

    void markOrderCompleted() {
        completedOrderCount++;
    }

    void markOrderCancelled() {
        cancelledOrderCount++;
    }

    public String describe() {
        return name + " (" + id + ") - " + mobile + " | wallet " + balance
                + " | " + tier().label() + " (" + completedOrderCount + " completed) | " + addresses.size() + " address(es)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Customer other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE.matcher(mobile.trim()).matches();
    }

    public static String normaliseMobile(String mobile) {
        if (mobile == null) {
            throw new IllegalArgumentException("a mobile number is required");
        }
        String digits = mobile.trim();
        if (!isValidMobile(digits)) {
            throw new IllegalArgumentException("a mobile number must be 11 digits starting with 010, 011, 012 or 015");
        }
        return digits;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
