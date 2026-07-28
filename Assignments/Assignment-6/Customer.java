public class Customer {

    private int id;
    private String name;
    private String phone;
    private int rentedCarId;
    private int rentedDays;
    private double totalPaid;

    private static int count = 0;

    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.rentedCarId = -1;
        this.rentedDays = 0;
        this.totalPaid = 0.0;
        count++;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPhone() {
        return this.phone;
    }

    public int getRentedCarId() {
        return this.rentedCarId;
    }

    public int getRentedDays() {
        return this.rentedDays;
    }

    public double getTotalPaid() {
        return this.totalPaid;
    }

    public static int getCount() {
        return count;
    }

    public void setRentedCarId(int rentedCarId) {
        this.rentedCarId = rentedCarId;
    }

    public void setRentedDays(int rentedDays) {
        this.rentedDays = rentedDays;
    }

    public void addPayment(double amount) {
        this.totalPaid += amount;
    }

    public boolean hasCar() {
        return this.rentedCarId != -1;
    }

    public void clearRental() {
        this.rentedCarId = -1;
        this.rentedDays = 0;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | Phone: %s | Total Paid: $%.2f",
                this.id, this.name, this.phone, this.totalPaid);
    }
}