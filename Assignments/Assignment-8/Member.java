public class Member {
    private static final int MAX_ITEMS = 3;
    private static final double MAX_DEBT_FOR_BORROWING = 100.00;
    private static final double EPSILON = 0.0000001;

    private String name;
    private final String membershipId;
    private final MembershipType membershipType;
    private double balance;
    private int itemsHeld;

    public Member(String name, String membershipId, MembershipType membershipType) {
        this(name, membershipId, membershipType, 0.00);
    }

    public Member(String name, String membershipId, MembershipType membershipType, double balance) {
        if (name == null) {
            name = "";
        }

        if (membershipId == null) {
            membershipId = "";
        }

        if (membershipType == null) {
            throw new IllegalArgumentException("Membership type is required.");
        }

        if (balance < 0.00) {
            throw new IllegalArgumentException("Starting balance cannot be negative.");
        }

        this.name = name.trim();
        this.membershipId = membershipId.trim();
        this.membershipType = membershipType;
        this.balance = round(balance);
        this.itemsHeld = 0;
    }

    public String getName() {
        return name;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public double getBalance() {
        return balance;
    }

    public int getItemsHeld() {
        return itemsHeld;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }

        this.name = name.trim();
    }

    public boolean chargeFine(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0.00) {
            return false;
        }

        balance = round(balance + amount);
        return true;
    }

    public boolean payFine(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)
                || amount <= 0.00
                || amount > balance + EPSILON) {
            return false;
        }

        balance = round(balance - amount);

        if (balance < 0.00 && balance > -0.01) {
            balance = 0.00;
        }

        return true;
    }

    public boolean canBorrow() {
        return itemsHeld < MAX_ITEMS && balance <= MAX_DEBT_FOR_BORROWING;
    }

    public void recordBorrow() {
        itemsHeld++;
    }

    public boolean recordReturn() {
        if (itemsHeld == 0) {
            return false;
        }

        itemsHeld--;
        return true;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}