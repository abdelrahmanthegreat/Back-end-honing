import java.util.Scanner;

public class Main {

    private static final int MAX_CARS = 20;
    private static final int MAX_CUSTOMERS = 20;

    private static Car[] cars = new Car[MAX_CARS];
    private static Customer[] customers = new Customer[MAX_CUSTOMERS];
    private static int carIndex = 0;
    private static int customerIndex = 0;
    private static double totalIncome = 0.0;

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:  addRegularCar();   break;
                case 2:  addLuxuryCar();    break;
                case 3:  addCustomer();     break;
                case 4:  displayAllCars();  break;
                case 5:  displayAvailableCars(); break;
                case 6:  rentCar();         break;
                case 7:  returnCar();       break;
                case 8:  searchCarById();   break;
                case 9:  searchCarByBrand(); break;
                case 10: displayAllCustomers(); break;
                case 0:  running = false;   break;
                default:
                    System.out.println("\n>> Invalid choice. Please try again.\n");
            }
        }

        printGoodbye();
        scanner.close();
    }

    private static void printBanner() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   WELCOME TO SPEEDWAY RENTALS SYSTEM     ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("       SPEEDWAY RENTALS SYSTEM");
        System.out.println("========================================");
        System.out.println("1.  Add Regular Car");
        System.out.println("2.  Add Luxury Car");
        System.out.println("3.  Add Customer");
        System.out.println("4.  Display All Cars");
        System.out.println("5.  Display Available Cars");
        System.out.println("6.  Rent a Car");
        System.out.println("7.  Return a Car");
        System.out.println("8.  Search Car by ID");
        System.out.println("9.  Search Car by Brand");
        System.out.println("10. Display All Customers");
        System.out.println("0.  Exit");
        System.out.println("========================================");
    }

    private static void printGoodbye() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║        THANK YOU — GOODBYE!              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Total cars registered : " + Car.getCarCount());
        System.out.println("  Total customers       : " + Customer.getCount());
        System.out.printf("  Total income          : $%.2f%n", totalIncome);
        System.out.println("  Cars currently rented : " + countRentedCars());
        System.out.printf("  Most expensive car    : %s%n", getMostExpensiveCar());
        System.out.printf("  Average daily price   : $%.2f%n", getAverageDailyPrice());
        System.out.println();
    }

    private static void addRegularCar() {
        if (carIndex >= MAX_CARS) {
            System.out.println("\n>> Error: Fleet is full (max " + MAX_CARS + " cars).\n");
            return;
        }

        int id = readInt("Car ID: ");
        if (findCarById(id) != null) {
            System.out.println(">> Error: Car ID " + id + " already exists.\n");
            return;
        }

        String brand = readString("Brand: ");
        String model = readString("Model: ");

        int year = readInt("Year (1990-2026): ");
        if (year < 1990 || year > 2026) {
            System.out.println(">> Error: Year must be between 1990 and 2026.\n");
            return;
        }

        double price = readDouble("Price per day: ");
        if (price <= 0) {
            System.out.println(">> Error: Price per day must be greater than zero.\n");
            return;
        }

        cars[carIndex] = new Car(id, brand, model, year, price);
        carIndex++;
        System.out.println("\n>> Regular car added successfully! [ID: " + id + "]\n");
    }

    private static void addLuxuryCar() {
        if (carIndex >= MAX_CARS) {
            System.out.println("\n>> Error: Fleet is full (max " + MAX_CARS + " cars).\n");
            return;
        }

        int id = readInt("Car ID: ");
        if (findCarById(id) != null) {
            System.out.println(">> Error: Car ID " + id + " already exists.\n");
            return;
        }

        String brand = readString("Brand: ");
        String model = readString("Model: ");

        int year = readInt("Year (1990-2026): ");
        if (year < 1990 || year > 2026) {
            System.out.println(">> Error: Year must be between 1990 and 2026.\n");
            return;
        }

        double price = readDouble("Price per day: ");
        if (price <= 0) {
            System.out.println(">> Error: Price per day must be greater than zero.\n");
            return;
        }

        double insurance = readDouble("Insurance fee: ");
        if (insurance < 0) {
            System.out.println(">> Error: Insurance fee cannot be negative.\n");
            return;
        }

        cars[carIndex] = new LuxuryCar(id, brand, model, year, price, insurance);
        carIndex++;
        System.out.println("\n>> Luxury car added successfully! [ID: " + id + "]\n");
    }

    private static void addCustomer() {
        if (customerIndex >= MAX_CUSTOMERS) {
            System.out.println("\n>> Error: Customer list is full (max " + MAX_CUSTOMERS + ").\n");
            return;
        }

        int id = readInt("Customer ID: ");
        if (findCustomerById(id) != null) {
            System.out.println(">> Error: Customer ID " + id + " already exists.\n");
            return;
        }

        String name = readString("Name: ");
        String phone = readString("Phone: ");

        customers[customerIndex] = new Customer(id, name, phone);
        customerIndex++;
        System.out.println("\n>> Customer \"" + name + "\" registered! [ID: " + id + "]\n");
    }

    private static void displayAllCars() {
        if (carIndex == 0) {
            System.out.println("\n>> The fleet is empty. No cars registered yet.\n");
            return;
        }
        System.out.println("\n--- ALL CARS (" + carIndex + ") ---");
        for (int i = 0; i < carIndex; i++) {
            System.out.println("  " + (i + 1) + ". " + cars[i]);
        }
        System.out.println();
    }

    private static void displayAvailableCars() {
        int count = 0;
        System.out.println("\n--- AVAILABLE CARS ---");
        for (int i = 0; i < carIndex; i++) {
            if (cars[i].isAvailable()) {
                count++;
                System.out.println("  " + count + ". " + cars[i]);
            }
        }
        if (count == 0) {
            System.out.println("  No cars are currently available.");
        }
        System.out.println("  Total available: " + count + "\n");
    }

    private static void rentCar() {
        int custId = readInt("Customer ID: ");
        Customer customer = findCustomerById(custId);
        if (customer == null) {
            System.out.println(">> Error: Customer ID " + custId + " not found.\n");
            return;
        }

        if (customer.hasCar()) {
            System.out.println(">> Error: Customer \"" + customer.getName()
                    + "\" already holds a car (ID: " + customer.getRentedCarId() + ").\n");
            return;
        }

        int carId = readInt("Car ID: ");
        Car car = findCarById(carId);
        if (car == null) {
            System.out.println(">> Error: Car ID " + carId + " not found.\n");
            return;
        }

        if (!car.isAvailable()) {
            System.out.println(">> Error: Car ID " + carId + " is already rented.\n");
            return;
        }

        int days = readInt("Number of rental days: ");
        if (days <= 0) {
            System.out.println(">> Error: Number of days must be greater than zero.\n");
            return;
        }

        if (car instanceof LuxuryCar) {
            LuxuryCar luxury = (LuxuryCar) car;
            if (days < LuxuryCar.getMinRentalDays()) {
                System.out.println(">> Error: Luxury cars require a minimum of "
                        + LuxuryCar.getMinRentalDays() + " days.\n");
                return;
            }
        }

        double cost = car.calculateCost(days);
        car.setAvailable(false);
        customer.setRentedCarId(carId);
        customer.setRentedDays(days);
        customer.addPayment(cost);
        totalIncome += cost;

        System.out.println("\n┌─────────── RENTAL RECEIPT ───────────┐");
        System.out.printf("│ Customer : %s%n", customer.getName());
        System.out.printf("│ Car      : %s %s [%s]%n", car.getBrand(), car.getModel(), car.getType());
        System.out.printf("│ Days     : %d%n", days);
        System.out.printf("│ Total    : $%.2f (incl. %.0f%% tax)%n", cost, Car.getTaxRate() * 100);
        System.out.println("└──────────────────────────────────────┘\n");
    }

    private static void returnCar() {
        int custId = readInt("Customer ID: ");
        Customer customer = findCustomerById(custId);
        if (customer == null) {
            System.out.println(">> Error: Customer ID " + custId + " not found.\n");
            return;
        }

        if (!customer.hasCar()) {
            System.out.println(">> Error: Customer \"" + customer.getName()
                    + "\" has no car to return.\n");
            return;
        }

        Car car = findCarById(customer.getRentedCarId());
        if (car != null) {
            car.setAvailable(true);
        }

        String carDesc = (car != null)
                ? car.getBrand() + " " + car.getModel()
                : "ID " + customer.getRentedCarId();

        customer.clearRental();
        System.out.println("\n>> Car returned successfully: " + carDesc
                + " | Customer: " + customer.getName() + "\n");
    }

    private static void searchCarById() {
        int id = readInt("Car ID to search: ");
        Car car = findCarById(id);
        if (car == null) {
            System.out.println("\n>> Car with ID " + id + " not found.\n");
        } else {
            System.out.println("\n>> Found: " + car + "\n");
        }
    }

    private static void searchCarByBrand() {
        String brand = readString("Brand to search: ");
        int matches = 0;

        System.out.println("\n--- RESULTS FOR \"" + brand.toUpperCase() + "\" ---");
        for (int i = 0; i < carIndex; i++) {
            if (cars[i].getBrand().equalsIgnoreCase(brand)) {
                matches++;
                System.out.println("  " + matches + ". " + cars[i]);
            }
        }

        if (matches == 0) {
            System.out.println("  No cars found with brand \"" + brand + "\".");
        } else {
            System.out.println("  Matches: " + matches);
        }
        System.out.println();
    }

    private static void displayAllCustomers() {
        if (customerIndex == 0) {
            System.out.println("\n>> No customers registered yet.\n");
            return;
        }

        System.out.println("\n--- ALL CUSTOMERS (" + customerIndex + ") ---");
        for (int i = 0; i < customerIndex; i++) {
            Customer c = customers[i];
            String carInfo;
            if (c.hasCar()) {
                Car car = findCarById(c.getRentedCarId());
                carInfo = (car != null)
                        ? car.getBrand() + " " + car.getModel() + " (" + c.getRentedDays() + " days)"
                        : "Car ID " + c.getRentedCarId();
            } else {
                carInfo = "None";
            }
            System.out.printf("  %d. %s | Rented: %s | Paid: $%.2f%n",
                    i + 1, c, carInfo, c.getTotalPaid());
        }
        System.out.println();
    }

    private static int countRentedCars() {
        int count = 0;
        for (int i = 0; i < carIndex; i++) {
            if (!cars[i].isAvailable()) count++;
        }
        return count;
    }

    private static String getMostExpensiveCar() {
        if (carIndex == 0) return "N/A";
        Car most = cars[0];
        for (int i = 1; i < carIndex; i++) {
            if (cars[i].getPricePerDay() > most.getPricePerDay()) {
                most = cars[i];
            }
        }
        return most.getBrand() + " " + most.getModel() + " ($" +
                String.format("%.2f", most.getPricePerDay()) + "/day)";
    }

    private static double getAverageDailyPrice() {
        if (carIndex == 0) return 0.0;
        double sum = 0;
        for (int i = 0; i < carIndex; i++) {
            sum += cars[i].getPricePerDay();
        }
        return sum / carIndex;
    }

    private static Car findCarById(int id) {
        for (int i = 0; i < carIndex; i++) {
            if (cars[i].getId() == id) {
                return cars[i];
            }
        }
        return null;
    }

    private static Customer findCustomerById(int id) {
        for (int i = 0; i < customerIndex; i++) {
            if (customers[i].getId() == id) {
                return customers[i];
            }
        }
        return null;
    }

    private static int readInt(String prompt) {
        System.out.print("  " + prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("  Invalid input. " + prompt);
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    private static double readDouble(String prompt) {
        System.out.print("  " + prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("  Invalid input. " + prompt);
            scanner.next();
        }
        double val = scanner.nextDouble();
        scanner.nextLine();
        return val;
    }

    private static String readString(String prompt) {
        System.out.print("  " + prompt);
        return scanner.nextLine().trim();
    }
}