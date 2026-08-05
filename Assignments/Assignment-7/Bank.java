import java.util.Locale;

public class Bank {
    private final Customer[] customers;
    private final Account[] accounts;
    private int customerCount;
    private int accountCount;
    private int nextCustomerId;
    private int nextAccountSequence;

    public Bank() {
        customers = new Customer[BankConfig.MAX_CUSTOMERS];
        accounts = new Account[BankConfig.MAX_ACCOUNTS];
        customerCount = 0;
        accountCount = 0;
        nextCustomerId = BankConfig.FIRST_CUSTOMER_ID;
        nextAccountSequence = BankConfig.FIRST_ACCOUNT_SEQUENCE;
    }

    public String registerCustomer(String name, String nationalId, String phone, CustomerTier tier) {
        if (customerCount >= BankConfig.MAX_CUSTOMERS) {
            return "Customer storage is full.";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty.";
        }
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return "National ID cannot be empty.";
        }
        if (tier == null) {
            return "Customer tier is required.";
        }

        String cleanPhone = phone == null ? "" : phone.trim();
        if (!cleanPhone.isEmpty() && !cleanPhone.matches("\\d{7,15}")) {
            return "Phone number must contain only digits and be between 7 and 15 digits.";
        }

        String cleanNationalId = nationalId.trim();
        if (findCustomerByNationalId(cleanNationalId) != null) {
            return "National ID already belongs to another customer.";
        }

        Customer customer = new Customer(nextCustomerId++, name.trim(), cleanNationalId, cleanPhone, tier);
        customers[customerCount++] = customer;

        return "Customer registered successfully.\n" + formatCustomer(customer);
    }

    public String openAccount(int customerId, AccountType type, double openingBalance, int durationMonths) {
        if (accountCount >= BankConfig.MAX_ACCOUNTS) {
            return "Account storage is full.";
        }

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            return "Customer ID not found.";
        }

        if (type == null) {
            return "Account type is required.";
        }

        if (!isFinite(openingBalance)) {
            return "Opening balance must be a finite number.";
        }

        double minimum = getMinimumOpeningBalance(type);
        if (openingBalance < minimum) {
            return "Minimum opening balance for " + type.getDisplayName() + " is " + money(minimum) + ".";
        }

        if (type == AccountType.FIXED_DEPOSIT && durationMonths < 0) {
            return "Duration cannot be negative.";
        }

        String accountNumber = "ACC" + nextAccountSequence++;
        Account account;

        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(accountNumber, customer.getId(), openingBalance);
        } else if (type == AccountType.CURRENT) {
            account = new CurrentAccount(accountNumber, customer.getId(), openingBalance);
        } else {
            account = new FixedDepositAccount(accountNumber, customer.getId(), openingBalance, durationMonths);
        }

        accounts[accountCount++] = account;
        customer.incrementAccountCount();

        return "Account opened successfully.\n" + account.getDetails(customer.getTier());
    }

    public String deposit(String accountNumber, double amount) {
        Account account = findAccountByNumber(accountNumber);
        if (account == null) {
            return "Account not found.";
        }

        String error = account.getDepositError(amount);
        if (error != null) {
            return error;
        }

        double oldBalance = account.getBalance();
        if (!account.deposit(amount)) {
            return "Deposit failed.";
        }

        return "Current balance = " + money(oldBalance)
                + ". Deposit = " + money(amount)
                + ". New balance = " + money(account.getBalance()) + ".";
    }

    public String withdraw(String accountNumber, double amount) {
        Account account = findAccountByNumber(accountNumber);
        if (account == null) {
            return "Account not found.";
        }

        String error = account.getWithdrawError(amount);
        if (error != null) {
            return error;
        }

        double oldBalance = account.getBalance();
        if (!account.withdraw(amount)) {
            return "Withdrawal failed.";
        }

        return "Current balance = " + money(oldBalance)
                + ". Withdrawal = " + money(amount)
                + ". New balance = " + money(account.getBalance()) + ".";
    }

    public String transfer(String sourceNumber, String destinationNumber, double amount) {
        Account source = findAccountByNumber(sourceNumber);
        Account destination = findAccountByNumber(destinationNumber);

        if (source == null) {
            return "Source account not found.";
        }
        if (destination == null) {
            return "Destination account not found.";
        }
        if (source.getAccountNumber().equalsIgnoreCase(destination.getAccountNumber())) {
            return "Source and destination accounts must be different.";
        }

        String sourceError = source.getWithdrawError(amount);
        if (sourceError != null) {
            return "Source account: " + sourceError;
        }

        String destinationError = destination.getDepositError(amount);
        if (destinationError != null) {
            return "Destination account: " + destinationError;
        }

        double oldSourceBalance = source.getBalance();
        int oldSourceTransactions = source.getTransactionCount();
        int oldMonthlyWithdrawals = source instanceof SavingsAccount
                ? ((SavingsAccount) source).getMonthlyWithdrawals()
                : 0;

        if (!source.withdraw(amount)) {
            return "Source withdrawal failed.";
        }

        if (!destination.deposit(amount)) {
            source.restoreState(oldSourceBalance, oldSourceTransactions, oldMonthlyWithdrawals);
            return "Transfer failed. Destination could not receive the amount. Money restored to source.";
        }

        return "Transfer successful. Source new balance = " + money(source.getBalance())
                + ". Destination new balance = " + money(destination.getBalance()) + ".";
    }

    public String getCustomerAccountsReport(int customerId) {
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            return "Customer ID not found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(formatCustomer(customer)).append("\nAccounts:\n");

        boolean found = false;
        double total = 0.0;

        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getOwnerId() == customerId) {
                found = true;
                total += accounts[i].getBalance();
                sb.append(formatAccountLine(accounts[i])).append('\n');
            }
        }

        if (!found) {
            sb.append("No accounts found for this customer.\n");
        }

        sb.append("Combined balance: ").append(money(total));
        return sb.toString();
    }

    public String getAllAccountsReport() {
        if (accountCount == 0) {
            return "No accounts found.";
        }

        StringBuilder sb = new StringBuilder("All branch accounts:\n");

        for (int i = 0; i < accountCount; i++) {
            Account account = accounts[i];
            Customer owner = findCustomerById(account.getOwnerId());
            String ownerName = owner == null ? "Unknown" : owner.getName();

            sb.append(account.getAccountNumber())
                    .append(" | Owner: ").append(ownerName)
                    .append(" | Type: ").append(account.getType().getDisplayName())
                    .append(" | Balance: ").append(money(account.getBalance()))
                    .append(" | Status: ").append(account.getStatus())
                    .append(" | Tx: ").append(account.getTransactionCount())
                    .append('\n');
        }

        return sb.toString().trim();
    }

    public String searchAccountByNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return "Account number cannot be empty.";
        }

        Account account = findAccountByNumber(accountNumber.trim());
        if (account == null) {
            return "Account not found.";
        }

        Customer owner = findCustomerById(account.getOwnerId());
        CustomerTier tier = owner == null ? CustomerTier.STANDARD : owner.getTier();
        String ownerName = owner == null ? "Unknown" : owner.getName();

        return "Owner: " + ownerName + "\n" + account.getDetails(tier);
    }

    public String searchAccountsByType(AccountType type) {
        if (type == null) {
            return "Account type is required.";
        }

        StringBuilder sb = new StringBuilder("Accounts of type " + type.getDisplayName() + ":\n");
        int count = 0;
        double total = 0.0;

        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getType() == type) {
                count++;
                total += accounts[i].getBalance();
                sb.append(formatAccountLine(accounts[i])).append('\n');
            }
        }

        if (count == 0) {
            sb.append("No accounts found.\n");
        }

        sb.append("Matching accounts: ").append(count).append('\n');
        sb.append("Combined balance: ").append(money(total));

        return sb.toString();
    }

    public String closeAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account == null) {
            return "Account not found.";
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            return "Account is already closed.";
        }

        if (Math.abs(account.getBalance()) > 0.0000001) {
            return "Account balance must be exactly $0.00 before closing.";
        }

        if (account instanceof FixedDepositAccount) {
            FixedDepositAccount fixed = (FixedDepositAccount) account;
            if (!fixed.isMatured()) {
                return "Fixed deposit is still locked. " + fixed.getRemainingMonths() + " month(s) remain.";
            }
        }

        Customer owner = findCustomerById(account.getOwnerId());
        account.setStatus(AccountStatus.CLOSED);

        if (owner != null) {
            owner.decrementAccountCount();
        }

        return "Account closed successfully.";
    }

    private Customer findCustomerById(int id) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getId() == id) {
                return customers[i];
            }
        }
        return null;
    }

    private Customer findCustomerByNationalId(String nationalId) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getNationalId().equals(nationalId)) {
                return customers[i];
            }
        }
        return null;
    }

    private Account findAccountByNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        String cleaned = accountNumber.trim();

        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equalsIgnoreCase(cleaned)) {
                return accounts[i];
            }
        }

        return null;
    }

    private double getMinimumOpeningBalance(AccountType type) {
        switch (type) {
            case SAVINGS:
                return BankConfig.SAVINGS_MIN_OPENING;
            case CURRENT:
                return BankConfig.CURRENT_MIN_OPENING;
            case FIXED_DEPOSIT:
                return BankConfig.FIXED_MIN_OPENING;
            default:
                return 0.0;
        }
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private String formatCustomer(Customer customer) {
        return "Customer ID: " + customer.getId()
                + "\nName: " + customer.getName()
                + "\nNational ID: " + customer.getNationalId()
                + "\nPhone: " + (customer.getPhone().isEmpty() ? "N/A" : customer.getPhone())
                + "\nTier: " + customer.getTier()
                + "\nMonthly fee: " + money(customer.getTier().getMonthlyFee())
                + "\nInterest bonus: " + String.format(Locale.US, "%.2f%%", customer.getTier().getInterestBonus())
                + "\nOpen accounts: " + customer.getAccountCount();
    }

    private String formatAccountLine(Account account) {
        return account.getAccountNumber()
                + " | " + account.getType().getDisplayName()
                + " | Balance: " + money(account.getBalance())
                + " | Status: " + account.getStatus()
                + " | Tx: " + account.getTransactionCount();
    }

    private String money(double value) {
        return String.format(Locale.US, "$%.2f", value);
    }
}