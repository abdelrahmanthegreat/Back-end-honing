public class Customer {
    private final int id;
    private final String name;
    private final String nationalId;
    private final String phone;
    private final CustomerTier tier;
    private int accountCount;

    public Customer(int id, String name, String nationalId, String phone, CustomerTier tier) {
        this.id = id;
        this.name = name;
        this.nationalId = nationalId;
        this.phone = phone == null ? "" : phone;
        this.tier = tier;
        this.accountCount = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getPhone() {
        return phone;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public int getAccountCount() {
        return accountCount;
    }

    public void incrementAccountCount() {
        accountCount++;
    }

    public void decrementAccountCount() {
        if (accountCount > 0) {
            accountCount--;
        }
    }
}