public class DVD extends LibraryItem {
    private static final int LOAN_PERIOD_DAYS = 3;
    private static final double DAILY_FINE = 15.00;

    private final int runtimeMinutes;

    public DVD(String itemId, String title, int runtimeMinutes) {
        super(itemId, title);
        this.runtimeMinutes = runtimeMinutes;
    }

    public int getRuntimeMinutes() {
        return runtimeMinutes;
    }

    @Override
    public String getCategoryName() {
        return "DVD";
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

        return DAILY_FINE * daysOverdue;
    }
}