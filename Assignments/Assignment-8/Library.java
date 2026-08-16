public class Library {
    private static final int ITEM_CAPACITY = 100;
    private static final int MEMBER_CAPACITY = 100;
    private static final int PROJECTED_OVERDUE_DAYS = 5;

    private final LibraryItem[] items = new LibraryItem[ITEM_CAPACITY];
    private final Member[] members = new Member[MEMBER_CAPACITY];

    private int itemCount = 0;
    private int memberCount = 0;

    public boolean registerItem(LibraryItem item) {
        if (item == null || itemCount == items.length) {
            return false;
        }

        if (findItemById(item.getItemId()) != null) {
            return false;
        }

        items[itemCount++] = item;
        LibraryItem.recordCatalogued();
        return true;
    }

    public boolean registerMember(Member member) {
        if (member == null || memberCount == members.length) {
            return false;
        }

        if (findMemberById(member.getMembershipId()) != null) {
            return false;
        }

        members[memberCount++] = member;
        return true;
    }

    public LibraryItem findItemById(String itemId) {
        if (itemId == null) {
            return null;
        }

        for (int i = 0; i < itemCount; i++) {
            if (items[i].getItemId().equals(itemId)) {
                return items[i];
            }
        }

        return null;
    }

    public Member findMemberById(String membershipId) {
        if (membershipId == null) {
            return null;
        }

        for (int i = 0; i < memberCount; i++) {
            if (members[i].getMembershipId().equals(membershipId)) {
                return members[i];
            }
        }

        return null;
    }

    private Member findMemberByName(String name) {
        if (name == null) {
            return null;
        }

        for (int i = 0; i < memberCount; i++) {
            if (members[i].getName().equals(name)) {
                return members[i];
            }
        }

        return null;
    }

    public void printCatalogue() {
        if (itemCount == 0) {
            System.out.println("No items in catalogue.");
            return;
        }

        for (int i = 0; i < itemCount; i++) {
            items[i].displayInfo();
        }
    }

    public void printItemsByStatus(ItemStatus status) {
        boolean found = false;

        for (int i = 0; i < itemCount; i++) {
            if (items[i].getStatus() == status) {
                items[i].displayInfo();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No items have status " + status + ".");
        }
    }

    public void printMembers() {
        if (memberCount == 0) {
            System.out.println("No members registered.");
            return;
        }

        for (int i = 0; i < memberCount; i++) {
            Member member = members[i];

            System.out.printf(
                    "%-10s | %-20s | %-8s | Items held: %d | Balance: %6.2f EGP%n",
                    member.getMembershipId(),
                    member.getName(),
                    member.getMembershipType(),
                    member.getItemsHeld(),
                    member.getBalance()
            );
        }
    }

    public int countItemsOnLoan() {
        int count = 0;

        for (int i = 0; i < itemCount; i++) {
            if (items[i].getStatus() == ItemStatus.ON_LOAN) {
                count++;
            }
        }

        return count;
    }

    public double getLoanRate() {
        if (itemCount == 0) {
            return 0.0;
        }

        return countItemsOnLoan() * 100.0 / itemCount;
    }

    public double getTotalOutstanding() {
        double total = 0.0;

        for (int i = 0; i < memberCount; i++) {
            total += members[i].getBalance();
        }

        return round(total);
    }

    public double getProjectedFines(int daysOverdue) {
        if (daysOverdue <= 0) {
            return 0.0;
        }

        double total = 0.0;

        for (int i = 0; i < itemCount; i++) {
            if (items[i].getStatus() == ItemStatus.ON_LOAN) {
                total += items[i].calculateFine(daysOverdue);
            }
        }

        return round(total);
    }

    public void printReport() {
        System.out.printf("Catalogue size: %d%n", itemCount);
        System.out.printf("Items ever catalogued: %d%n", LibraryItem.getCataloguedCount());
        System.out.printf("Items on loan: %d%n", countItemsOnLoan());
        System.out.printf("Loan rate: %.2f%%%n", getLoanRate());
        System.out.printf("Total outstanding fines: %.2f EGP%n", getTotalOutstanding());
        System.out.printf(
                "Projected fines for %d-day overdue scenario (before waivers): %.2f EGP%n",
                PROJECTED_OVERDUE_DAYS,
                getProjectedFines(PROJECTED_OVERDUE_DAYS)
        );
    }

    public void borrowItem(String itemId, String membershipId) {
        LibraryItem item = findItemById(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        Member member = findMemberById(membershipId);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            System.out.println("Item cannot be borrowed because it is " + item.getStatus() + ".");
            return;
        }

        if (!member.canBorrow()) {
            System.out.println("Member cannot borrow. Maximum items: 3, maximum debt: 100.00 EGP.");
            return;
        }

        if (item.lendTo(member.getName())) {
            member.recordBorrow();
            System.out.println("Borrow successful. Loan period: " + item.getLoanPeriodDays() + " days.");
        } else {
            System.out.println("Borrow failed.");
        }
    }

    public void processReturn(String itemId, int daysOverdue) {
        LibraryItem item = findItemById(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        if (daysOverdue < 0) {
            System.out.println("Days overdue cannot be negative.");
            return;
        }

        if (item.getStatus() != ItemStatus.ON_LOAN) {
            System.out.println("Item cannot be returned because it is not on loan.");
            return;
        }

        Member borrower = findMemberByName(item.getBorrowerName());

        if (borrower == null) {
            System.out.println("Borrower record is missing. Return cannot be completed.");
            return;
        }

        double baseFine = round(item.calculateFine(daysOverdue));
        double waiverRate = borrower.getMembershipType().getWaiverRate();

        double waivedAmount = 0.0;
        double administrativeCharge = 0.0;
        double totalFine = 0.0;

        if (daysOverdue > 0) {
            double fineAfterWaiver = round(baseFine * (1.0 - waiverRate));
            waivedAmount = round(baseFine - fineAfterWaiver);
            administrativeCharge = LibraryItem.getAdministrativeCharge();
            totalFine = round(fineAfterWaiver + administrativeCharge);

            borrower.chargeFine(totalFine);
        }

        borrower.recordReturn();

        System.out.println("Return processed for item " + item.getItemId() + " (" + item.getTitle() + ").");
        System.out.printf("Days overdue: %d%n", daysOverdue);
        System.out.printf("Base fine: %.2f EGP%n", baseFine);

        if (daysOverdue > 0) {
            System.out.printf("Waiver (%.0f%%): -%.2f EGP%n", waiverRate * 100.0, waivedAmount);
            System.out.printf("Administrative charge: %.2f EGP%n", administrativeCharge);
            System.out.printf("Total fine charged: %.2f EGP%n", totalFine);
        } else {
            System.out.println("Returned on time. No fine charged.");
        }

        System.out.printf("Member balance: %.2f EGP%n", borrower.getBalance());

        item.takeBack();
    }

    public void renewLoan(String itemId) {
        LibraryItem item = findItemById(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        if (item instanceof Renewable) {
            Renewable renewable = (Renewable) item;

            if (renewable.renewLoan()) {
                int remaining = renewable.getRenewalLimit() - item.getRenewalCount();
                System.out.println("Renewal successful. Renewals remaining: " + remaining + ".");
            } else {
                if (item.getStatus() != ItemStatus.ON_LOAN) {
                    System.out.println("Renewal failed: item is not on loan.");
                } else if (item.getRenewalCount() >= renewable.getRenewalLimit()) {
                    System.out.println("Renewal failed: renewal limit reached.");
                } else {
                    System.out.println("Renewal failed.");
                }
            }
        } else {
            System.out.println("This item type cannot be renewed.");
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}