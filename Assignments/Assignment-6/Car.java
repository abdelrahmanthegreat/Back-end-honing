public class Car {

    private int id;
    private String brand;
    private String model;
    private int year;
    private double pricePerDay;
    private boolean available;

    private static int carCount = 0;
    private static final double TAX_RATE = 0.14;

    public Car(int id, String brand, String model, int year, double pricePerDay) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.available = true;
        carCount++;
    }

    public int getId() {
        return this.id;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public int getYear() {
        return this.year;
    }

    public double getPricePerDay() {
        return this.pricePerDay;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public static int getCarCount() {
        return carCount;
    }

    public static double getTaxRate() {
        return TAX_RATE;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double calculateCost(int days) {
        return this.pricePerDay * days * (1 + TAX_RATE);
    }

    public String getType() {
        return "Regular";
    }

    @Override
    public String toString() {
        String status = this.available ? "Available" : "Rented";
        return String.format("[%d] %s %s (%d) | $%.2f/day | %s | %s",
                this.id, this.brand, this.model, this.year,
                this.pricePerDay, status, getType());
    }
}