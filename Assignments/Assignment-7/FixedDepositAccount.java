import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class FixedDepositAccount extends Account {
    private final int durationMonths;
    private final YearMonth openingMonth;
    private final double annualInterestRate = BankConfig.FIXED_ANNUAL_INTEREST_RATE;

    public FixedDepositAccount(String accountNumber, int ownerId, double balance, int durationMonths) {
        super(accountNumber, ownerId, balance);
        this.durationMonths = durationMonths;
        this.openingMonth = YearMonth.now();
    }

    public int getMonthsPassed() {
        long passed = ChronoUnit.MONTHS.between(openingMonth, YearMonth.now());
        return passed > 0 ? (int) passed : 0;
    }

    public boolean isMatured() {
        return getMonthsPassed() >= durationMonths;
    }

    public int getRemainingMonths() {
        int remaining = durationMonths - getMonthsPassed();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    protected String getTypeWithdrawError(double amount) {
        if (!isMatured()) {
            return "Fixed deposit is locked. " + getRemainingMonths() + " month(s) remain.";
        }
        if (getBalance() - amount < 0) {
            return "Insufficient funds. Fixed deposit balance cannot become negative.";
        }
        return null;
    }

    @Override
    public AccountType getType() {
        return AccountType.FIXED_DEPOSIT;
    }

    @Override
    public String getDetails(CustomerTier ownerTier) {
        double bonus = ownerTier == null ? 0.0 : ownerTier.getInterestBonus();
        int monthsPassed = getMonthsPassed();

        String details = getCommonDetails()
                + "\nDuration: " + durationMonths + " month(s)"
                + "\nMonths passed: " + monthsPassed
                + "\nMatured: " + (isMatured() ? "Yes" : "No");

        if (!isMatured()) {
            details += "\nRemaining months: " + getRemainingMonths();
        }

        details += "\nAnnual interest rate: " + formatPercent(annualInterestRate + bonus);
        return details;
    }
}