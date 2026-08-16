public abstract class LibraryItem {
    private static final String LIBRARY_NAME = "Bayt Al Hekma";
    private static final double ADMINISTRATIVE_CHARGE = 10.00;
    private static int cataloguedCount = 0;

    private final String itemId;
    private final String title;
    private ItemStatus status;
    private String borrowerName;
    private int renewalCount;

    protected LibraryItem(String itemId, String title) {
        if (itemId == null) {
            itemId = "";
        }

        if (title == null) {
            title = "";
        }

        this.itemId = itemId.trim();
        this.title = title.trim();
        this.status = ItemStatus.AVAILABLE;
        this.borrowerName = null;
        this.renewalCount = 0;
    }

    static void recordCatalogued() {
        cataloguedCount++;
    }

    public static String getLibraryName() {
        return LIBRARY_NAME;
    }

    public static double getAdministrativeCharge() {
        return ADMINISTRATIVE_CHARGE;
    }

    public static int getCataloguedCount() {
        return cataloguedCount;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public boolean markReserved() {
        if (status != ItemStatus.AVAILABLE) {
            return false;
        }

        status = ItemStatus.RESERVED;
        return true;
    }

    public boolean markLost() {
        if (status != ItemStatus.AVAILABLE && status != ItemStatus.RESERVED) {
            return false;
        }

        status = ItemStatus.LOST;
        return true;
    }

    public boolean bringBack() {
        if (status != ItemStatus.RESERVED && status != ItemStatus.LOST) {
            return false;
        }

        status = ItemStatus.AVAILABLE;
        return true;
    }

    public boolean lendTo(String borrowerName) {
        if (status != ItemStatus.AVAILABLE) {
            return false;
        }

        if (borrowerName == null) {
            borrowerName = "";
        }

        this.status = ItemStatus.ON_LOAN;
        this.borrowerName = borrowerName.trim();
        this.renewalCount = 0;
        return true;
    }

    public final boolean takeBack() {
        if (status != ItemStatus.ON_LOAN) {
            return false;
        }

        status = ItemStatus.AVAILABLE;
        borrowerName = null;
        renewalCount = 0;
        return true;
    }

    protected boolean recordRenewal() {
        if (status != ItemStatus.ON_LOAN) {
            return false;
        }

        renewalCount++;
        return true;
    }

    public abstract String getCategoryName();

    public abstract int getLoanPeriodDays();

    public abstract double calculateFine(int daysOverdue);

    public void displayInfo() {
        System.out.printf(
                "%-8s | %-8s | %-30s | %-9s | %-12s | %2d-day loan | %6.2f EGP%n",
                itemId,
                getCategoryName(),
                title,
                status,
                borrowerName == null || borrowerName.isEmpty() ? "-" : borrowerName,
                getLoanPeriodDays(),
                calculateFine(1)
        );
    }
}