public class CurrentAccount extends Account {
    private final double overdraftLimit = BankConfig.CURRENT_OVERDRAFT_LIMIT;

    public CurrentAccount(String accountNumber, int ownerId, double balance) {
        super(accountNumber, ownerId, balance);
    }

    public boolean isOverdraftUsed() {
        return getBalance() < 0;
    }

    @Override
    protected String getTypeWithdrawError(double amount) {
        if (getBalance() - amount < -overdraftLimit) {
            return "Withdrawal exceeds overdraft limit. Minimum allowed balance is "
                    + formatMoney(-overdraftLimit) + ".";
        }
        return null;
    }

    @Override
    public AccountType getType() {
        return AccountType.CURRENT;
    }

    @Override
    public String getDetails(CustomerTier ownerTier) {
        return getCommonDetails()
                + "\nOverdraft limit: " + formatMoney(overdraftLimit)
                + "\nOverdraft used: " + (isOverdraftUsed() ? "Yes" : "No");
    }
}