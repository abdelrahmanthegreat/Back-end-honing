public enum CustomerTier {
    STANDARD(5.0, 0.0),
    SILVER(10.0, 0.25),
    GOLD(25.0, 0.50);

    private final double monthlyFee;
    private final double interestBonus;

    CustomerTier(double monthlyFee, double interestBonus) {
        this.monthlyFee = monthlyFee;
        this.interestBonus = interestBonus;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public double getInterestBonus() {
        return interestBonus;
    }
}