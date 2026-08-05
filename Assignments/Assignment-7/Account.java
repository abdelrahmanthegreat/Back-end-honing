import java.util.Locale;

public abstract class Account {
    private final String accountNumber;
    private final int ownerId;
    private double balance;
    private AccountStatus status;
    private int transactionCount;

    public Account(String accountNumber, int ownerId, double balance) {
        this.accountNumber = accountNumber;
        this.ownerId = ownerId;
        this.balance = balance;
        this.status = AccountStatus.ACTIVE;
        this.transactionCount = 0;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean allowsTransactions() {
        return status == AccountStatus.ACTIVE;
    }

    protected boolean isValidAmount(double amount) {
        return !Double.isNaN(amount)
                && !Double.isInfinite(amount)
                && amount > 0
                && amount >= BankConfig.MIN_TRANSACTION_AMOUNT;
    }

    public String getDepositError(double amount) {
        if (status == AccountStatus.FROZEN) {
            return "Account is frozen.";
        }
        if (status == AccountStatus.CLOSED) {
            return "Account is closed.";
        }
        if (!isValidAmount(amount)) {
            return "Amount must be a positive finite number and at least "
                    + formatMoney(BankConfig.MIN_TRANSACTION_AMOUNT) + ".";
        }
        return null;
    }

    public String getWithdrawError(double amount) {
        if (status == AccountStatus.FROZEN) {
            return "Account is frozen.";
        }
        if (status == AccountStatus.CLOSED) {
            return "Account is closed.";
        }
        if (!isValidAmount(amount)) {
            return "Amount must be a positive finite number and at least "
                    + formatMoney(BankConfig.MIN_TRANSACTION_AMOUNT) + ".";
        }
        return getTypeWithdrawError(amount);
    }

    protected abstract String getTypeWithdrawError(double amount);

    public boolean deposit(double amount) {
        if (getDepositError(amount) != null) {
            return false;
        }
        balance += amount;
        transactionCount++;
        return true;
    }

    public boolean withdraw(double amount) {
        if (getWithdrawError(amount) != null) {
            return false;
        }
        balance -= amount;
        transactionCount++;
        onWithdrawSuccess();
        return true;
    }

    protected void onWithdrawSuccess() {
    }

    public abstract AccountType getType();

    public abstract String getDetails(CustomerTier ownerTier);

    protected String getCommonDetails() {
        return "Account Number: " + accountNumber
                + "\nOwner ID: " + ownerId
                + "\nType: " + getType().getDisplayName()
                + "\nBalance: " + formatMoney(balance)
                + "\nStatus: " + status
                + "\nTransactions: " + transactionCount;
    }

    protected String formatMoney(double value) {
        return String.format(Locale.US, "$%.2f", value);
    }

    protected String formatPercent(double value) {
        return String.format(Locale.US, "%.2f%%", value);
    }

    public void restoreState(double balance, int transactionCount, int monthlyWithdrawals) {
        this.balance = balance;
        this.transactionCount = transactionCount;
    }
}