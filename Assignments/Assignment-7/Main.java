public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        ConsoleUI ui = new ConsoleUI(bank);
        ui.run();
    }
}