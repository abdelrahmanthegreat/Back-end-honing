import java.util.Scanner;

public class ATMSimulation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        final int CORRECT_PIN = 1234;
        double currentBalance = 2500.75;
        int attempts = 0;
        boolean isAuthenticated = false;

        do {
            System.out.print("Enter your 4-digit PIN: ");
            int enteredPin = scanner.nextInt();
            attempts++;

            if (enteredPin == CORRECT_PIN) {
                isAuthenticated = true;
            } else if (attempts < 3) {
                System.out.println("Incorrect PIN. Attempts left: " + (3 - attempts));
            }
        } while (attempts < 3 && !isAuthenticated);

        if (!isAuthenticated) {
            System.out.println("Your account has been locked.");
            return;
        }

        int successfulDeposits = 0;
        int successfulWithdrawals = 0;
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n========= ATM =========");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Show Account Status");
            System.out.println("5. Exit");
            System.out.println("=======================");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.printf("Current Balance: $%.2f%n", currentBalance);
                    break;
                    
                case 2:
                    System.out.print("Enter deposit amount: ");
                    double depositAmount = scanner.nextDouble();
                    if (depositAmount > 0) {
                        currentBalance += depositAmount;
                        successfulDeposits++;
                        System.out.printf("Updated Balance: $%.2f%n", currentBalance);
                    } else {
                        System.out.println("Invalid amount.");
                    }
                    break;
                    
                case 3:
                    System.out.print("Enter withdrawal amount: ");
                    double withdrawAmount = scanner.nextDouble();
                    if (withdrawAmount == 0) {
                        System.out.println("Transaction cancelled.");
                    } else if (withdrawAmount < 0) {
                        System.out.println("Invalid amount.");
                    } else if (withdrawAmount > currentBalance) {
                        System.out.println("Insufficient balance.");
                    } else {
                        currentBalance -= withdrawAmount;
                        successfulWithdrawals++;
                        System.out.printf("Updated Balance: $%.2f%n", currentBalance);
                        if (currentBalance == 0) {
                            System.out.println("Warning: Your account is empty.");
                        }
                    }
                    break;
                    
                case 4:
                    if (currentBalance >= 5000) {
                        System.out.println("VIP Customer");
                    } else if (currentBalance >= 1000) {
                        System.out.println("Regular Customer");
                    } else {
                        System.out.println("Low Balance");
                    }
                    break;
                    
                case 5:
                    System.out.println("Thank you for using our ATM.");
                    System.out.println("Total successful deposits: " + successfulDeposits);
                    System.out.println("Total successful withdrawals: " + successfulWithdrawals);
                    isRunning = false;
                    break; 
                    
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
        scanner.close();
    }
}