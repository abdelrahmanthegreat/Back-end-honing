public enum MembershipType {
    REGULAR(0.00),
    STUDENT(0.25),
    SENIOR(0.50);

    private final double waiverRate;

    MembershipType(double waiverRate) {
        this.waiverRate = waiverRate;
    }

    public double getWaiverRate() {
        return waiverRate;
    }
}