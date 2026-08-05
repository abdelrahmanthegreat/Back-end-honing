import java.time.YearMonth;

public class SavingsAccount extends Account {
    private int monthlyWithdrawals;
    private YearMonth withdrawalMonth;
    private final double annualInterestRate = BankConfig.SAVINGS_ANNUAL_INTEREST_RATE;

    public SavingsAccount(String accountNumber, int ownerId, double balance) {
        super(accountNumber, ownerId, balance);
        this.monthlyWithdrawals = 0;
        this.withdrawalMonth = YearMonth.now();
    }

    public int getMonthlyWithdrawals() {
        return monthlyWithdrawals;
    }

    @Override
    protected String getTypeWithdrawError(double amount) {
        if (getBalance() - amount < 0) {
            return "Insufficient funds. Savings balance cannot become negative.";
        }
        return null;
    }

    @Override
    protected void onWithdrawSuccess() {
        YearMonth current = YearMonth.now();
        if (!current.equals(withdrawalMonth)) {
            withdrawalMonth = current;
            monthlyWithdrawals = 0;
        }
        monthlyWithdrawals++;
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    @Override
    public void restoreState(double balance, int transactionCount, int monthlyWithdrawals) {
        super.restoreState(balance, transactionCount, monthlyWithdrawals);
        this.monthlyWithdrawals = monthlyWithdrawals;
    }

    @Override
    public String getDetails(CustomerTier ownerTier) {
        double bonus = ownerTier == null ? 0.0 : ownerTier.getInterestBonus();
        return getCommonDetails()
                + "\nAnnual interest rate: " + formatPercent(annualInterestRate + bonus)
                + "\nMonthly withdrawals this month: " + monthlyWithdrawals;
    }
}