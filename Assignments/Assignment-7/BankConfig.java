public final class BankConfig {
    private BankConfig() {
    }

    public static final int MAX_CUSTOMERS = 100;
    public static final int MAX_ACCOUNTS = 300;

    public static final double MIN_TRANSACTION_AMOUNT = 1.0;

    public static final double SAVINGS_MIN_OPENING = 100.0;
    public static final double CURRENT_MIN_OPENING = 500.0;
    public static final double FIXED_MIN_OPENING = 1000.0;

    public static final double SAVINGS_ANNUAL_INTEREST_RATE = 3.5;
    public static final double FIXED_ANNUAL_INTEREST_RATE = 6.0;
    public static final double CURRENT_OVERDRAFT_LIMIT = 1000.0;

    public static final int FIRST_CUSTOMER_ID = 1001;
    public static final int FIRST_ACCOUNT_SEQUENCE = 100001;
}