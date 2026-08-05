public enum AccountType {
    SAVINGS("Savings"),
    CURRENT("Current"),
    FIXED_DEPOSIT("Fixed Deposit");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}