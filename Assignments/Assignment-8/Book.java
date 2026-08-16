public class Book extends LibraryItem implements Renewable {
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double DAILY_FINE = 5.00;
    private static final int RENEWAL_LIMIT = 2;

    private final String author;
    private final int pageCount;

    public Book(String itemId, String title, String author, int pageCount) {
        super(itemId, title);

        if (author == null) {
            author = "";
        }

        this.author = author.trim();
        this.pageCount = pageCount;
    }

    public String getAuthor() {
        return author;
    }

    public int getPageCount() {
        return pageCount;
    }

    @Override
    public String getCategoryName() {
        return "Book";
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