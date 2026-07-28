public class LuxuryCar extends Car {

    private double insuranceFee;
    private static final int MIN_RENTAL_DAYS = 3;

    public LuxuryCar(int id, String brand, String model, int year,
                     double pricePerDay, double insuranceFee) {
        super(id, brand, model, year, pricePerDay);
        this.insuranceFee = insuranceFee;
    }

    public LuxuryCar(int id, String brand, String model, int year,
                     double pricePerDay) {
        this(id, brand, model, year, pricePerDay, 0.0);
    }

    public double getInsuranceFee() {
        return this.insuranceFee;
    }

    public void setInsuranceFee(double insuranceFee) {
        this.insuranceFee = insuranceFee;
    }

    public static int getMinRentalDays() {
        return MIN_RENTAL_DAYS;
    }

    @Override
    public double calculateCost(int days) {
        return super.calculateCost(days) + this.insuranceFee;
    }

    @Override
    public String getType() {
        return "Luxury";
    }

    @Override
    public String toString() {
        String status = isAvailable() ? "Available" : "Rented";
        return String.format("[%d] %s %s (%d) | $%.2f/day | Insurance: $%.2f | Min %d days | %s | %s",
                getId(), getBrand(), getModel(), getYear(),
                getPricePerDay(), this.insuranceFee, MIN_RENTAL_DAYS,
                status, getType());
    }
}