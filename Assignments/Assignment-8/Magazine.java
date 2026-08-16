public class Magazine extends LibraryItem implements Renewable {
    private static final int LOAN_PERIOD_DAYS = 7;
    private static final double DAILY_FINE = 3.00;
    private static final double MAX_FINE = 30.00;
    private static final int RENEWAL_LIMIT = 1;

    private final int issueNumber;

    public Magazine(String itemId, String title, int issueNumber) {
        super(itemId, title);
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public String getCategoryName() {
        return "Magazine";
    }

    @Override
    public int getLoanPeriodDays() {
        return LOAN_PERIOD_DAYS;
    }

    @Override
    public double calculateFine(int daysOverdue) {
        if (daysOverdue <= 0) {
            return 0.00;
        }

        return Math.min(DAILY_FINE * daysOverdue, MAX_FINE);
    }

    @Override
    public boolean renewLoan() {
        if (getStatus() != ItemStatus.ON_LOAN) {
            return false;
        }

        if (getRenewalCount() >= getRenewalLimit()) {
            return false;
        }

        return recordRenewal();
    }

    @Override
    public int getRenewalLimit() {
        return RENEWAL_LIMIT;
    }
}