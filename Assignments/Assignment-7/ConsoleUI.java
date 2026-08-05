public class ConsoleUI {
    private final Bank bank;
    private final InputReader reader;

    public ConsoleUI(Bank bank) {
        this.bank = bank;
        this.reader = new InputReader();
    }

    public void run() {
        while (true) {
            displayMenu();

            Integer choice = reader.readInt("Enter choice: ");
            if (choice == null) {
                if (reader.isEof()) {
                    System.out.println("Goodbye.");
                    break;
                }
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 0:
                    System.out.println("Goodbye.");
                    return;
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    openAccount();
                    break;
                case 3:
                    deposit();
                    break;
                case 4:
                    withdraw();
                    break;
                case 5:
                    transfer();
                    break;
                case 6:
                    displayCustomerAccounts();
                    break;
                case 7:
                    displayAllBranchAccounts();
                    break;
                case 8:
                    searchAccountByNumber();
                    break;
                case 9:
                    searchAccountsByType();
                    break;
                case 10:
                    closeAccount();
                    break;
                default:
                    System.out.println("Invalid menu choice.");
            }

            if (reader.isEof()) {
                System.out.println("Goodbye.");
                break;
            }
        }
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("Al Manara Bank");
        System.out.println("1 Register New Customer");
        System.out.println("2 Open New Account");
        System.out.println("3 Deposit Money");
        System.out.println("4 Withdraw Money");
        System.out.println("5 Transfer Between Accounts");
        System.out.println("6 Display Customer Accounts");
        System.out.println("7 Display All Branch Accounts");
        System.out.println("8 Search Account by Number");
        System.out.println("9 Search Accounts by Type");
        System.out.println("10 Close an Account");
        System.out.println("0 Exit");
    }

    private void registerCustomer() {
        String name = reader.readLine("Full name: ");
        if (name == null) {
            return;
        }

        String nationalId = reader.readLine("National ID: ");
        if (nationalId == null) {
            return;
        }

        String phone = reader.readLine("Phone (optional): ");
        if (phone == null) {
            return;
        }

        System.out.println("Tier: 1=STANDARD, 2=SILVER, 3=GOLD");
        Integer tierChoice = reader.readInt("Tier: ");
        if (tierChoice == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        CustomerTier tier = toTier(tierChoice);
        if (tier == null) {
            invalid();
            return;
        }

        System.out.println(bank.registerCustomer(name, nationalId, phone, tier));
    }

    private void openAccount() {
        Integer customerId = reader.readInt("Customer ID: ");
        if (customerId == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        System.out.println("Account types: 1=Savings, 2=Current, 3=Fixed Deposit");
        Integer typeChoice = reader.readInt("Account type: ");
        if (typeChoice == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        AccountType type = toAccountType(typeChoice);
        if (type == null) {
            invalid();
            return;
        }

        Double openingBalance = reader.readDouble("Opening balance: ");
        if (openingBalance == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        int durationMonths = 0;
        if (type == AccountType.FIXED_DEPOSIT) {
            Integer duration = reader.readInt("Duration in months: ");
            if (duration == null) {
                if (!reader.isEof()) {
                    invalid();
                }
                return;
            }
            durationMonths = duration;
        }

        System.out.println(bank.openAccount(customerId, type, openingBalance, durationMonths));
    }

    private void deposit() {
        String accountNumber = readAccountNumber("Account number: ");
        if (accountNumber == null) {
            return;
        }

        Double amount = reader.readDouble("Amount: ");
        if (amount == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        System.out.println(bank.deposit(accountNumber, amount));
    }

    private void withdraw() {
        String accountNumber = readAccountNumber("Account number: ");
        if (accountNumber == null) {
            return;
        }

        Double amount = reader.readDouble("Amount: ");
        if (amount == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        System.out.println(bank.withdraw(accountNumber, amount));
    }

    private void transfer() {
        String source = readAccountNumber("Source account number: ");
        if (source == null) {
            return;
        }

        String destination = readAccountNumber("Destination account number: ");
        if (destination == null) {
            return;
        }

        Double amount = reader.readDouble("Amount: ");
        if (amount == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        System.out.println(bank.transfer(source, destination, amount));
    }

    private void displayCustomerAccounts() {
        Integer customerId = reader.readInt("Customer ID: ");
        if (customerId == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        System.out.println(bank.getCustomerAccountsReport(customerId));
    }

    private void displayAllBranchAccounts() {
        System.out.println(bank.getAllAccountsReport());
    }

    private void searchAccountByNumber() {
        String accountNumber = readAccountNumber("Account number: ");
        if (accountNumber == null) {
            return;
        }

        System.out.println(bank.searchAccountByNumber(accountNumber));
    }

    private void searchAccountsByType() {
        System.out.println("Account types: 1=Savings, 2=Current, 3=Fixed Deposit");

        Integer typeChoice = reader.readInt("Account type: ");
        if (typeChoice == null) {
            if (!reader.isEof()) {
                invalid();
            }
            return;
        }

        AccountType type = toAccountType(typeChoice);
        if (type == null) {
            invalid();
            return;
        }

        System.out.println(bank.searchAccountsByType(type));
    }

    private void closeAccount() {
        String accountNumber = readAccountNumber("Account number: ");
        if (accountNumber == null) {
            return;
        }

        System.out.println(bank.closeAccount(accountNumber));
    }

    private String readAccountNumber(String prompt) {
        String accountNumber = reader.readLine(prompt);
        if (accountNumber == null) {
            return null;
        }

        if (accountNumber.isEmpty()) {
            invalid();
            return null;
        }

        return accountNumber;
    }

    private CustomerTier toTier(int choice) {
        switch (choice) {
            case 1:
                return CustomerTier.STANDARD;
            case 2:
                return CustomerTier.SILVER;
            case 3:
                return CustomerTier.GOLD;
            default:
                return null;
        }
    }

    private AccountType toAccountType(int choice) {
        switch (choice) {
            case 1:
                return AccountType.SAVINGS;
            case 2:
                return AccountType.CURRENT;
            case 3:
                return AccountType.FIXED_DEPOSIT;
            default:
                return null;
        }
    }

    private void invalid() {
        System.out.println("Invalid input. Operation cancelled.");
    }
}