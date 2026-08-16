import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        Library library = new Library();

        library.registerItem(new Book("B001", "Clean Code", "Robert C. Martin", 464));
        library.registerItem(new Book("B002", "Introduction to Algorithms", "Thomas H. Cormen", 1312));
        library.registerItem(new Magazine("M001", "Nature", 632));
        library.registerItem(new Magazine("M002", "IEEE Spectrum", 61));
        library.registerItem(new DVD("D001", "The Social Dilemma", 94));
        library.registerItem(new DVD("D002", "Code Rush", 58));

        library.registerMember(new Member("Abdelrahman", "MEM001", MembershipType.STUDENT));
        library.registerMember(new Member("Sara", "MEM002", MembershipType.SENIOR));
        library.registerMember(new Member("Omar", "MEM003", MembershipType.REGULAR, 20.00));

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt(scanner, "Enter choice: ", 0, 10);

            switch (choice) {
                case 1: {
                    library.printCatalogue();
                    break;
                }

                case 2: {
                    String name = readRequiredString(scanner, "Enter member name: ");
                    String membershipId = readRequiredString(scanner, "Enter membership ID: ");
                    MembershipType type = readMembershipType(scanner);

                    Member member = new Member(name, membershipId, type);

                    if (library.registerMember(member)) {
                        System.out.println("Member registered.");
                    } else {
                        System.out.println("Member could not be registered. Register is full or ID already exists.");
                    }

                    break;
                }

                case 3: {
                    String borrowItemId = readRequiredString(scanner, "Enter item ID: ");
                    String borrowMemberId = readRequiredString(scanner, "Enter membership ID: ");
                    library.borrowItem(borrowItemId, borrowMemberId);
                    break;
                }

                case 4: {
                    String returnItemId = readRequiredString(scanner, "Enter item ID: ");
                    int daysOverdue = readNonNegativeInt(scanner, "Enter days overdue (0 = on time): ");
                    library.processReturn(returnItemId, daysOverdue);
                    break;
                }

                case 5: {
                    String renewItemId = readRequiredString(scanner, "Enter item ID: ");
                    library.renewLoan(renewItemId);
                    break;
                }

                case 6: {
                    String searchItemId = readRequiredString(scanner, "Enter item ID: ");
                    LibraryItem foundItem = library.findItemById(searchItemId);

                    if (foundItem == null) {
                        System.out.println("Item not found.");
                    } else {
                        foundItem.displayInfo();
                    }

                    break;
                }

                case 7: {
                    ItemStatus status = readItemStatus(scanner);
                    library.printItemsByStatus(status);
                    break;
                }

                case 8: {
                    String payMemberId = readRequiredString(scanner, "Enter membership ID: ");
                    double amount = readPositiveDouble(scanner, "Enter payment amount: ");

                    Member payingMember = library.findMemberById(payMemberId);

                    if (payingMember == null) {
                        System.out.println("Member not found.");
                    } else if (payingMember.payFine(amount)) {
                        System.out.printf("Payment accepted. New balance: %.2f EGP%n", payingMember.getBalance());
                    } else {
                        System.out.println("Payment rejected. Amount must be positive and cannot exceed balance owed.");
                    }

                    break;
                }

                case 9: {
                    library.printMembers();
                    break;
                }

                case 10: {
                    library.printReport();
                    break;
                }

                case 0: {
                    running = false;
                    System.out.println("Exiting.");
                    break;
                }

                default: {
                    System.out.println("Invalid choice.");
                    break;
                }
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Bayt Al Hekma Library Management System ===");
        System.out.println("1. View catalogue");
        System.out.println("2. Register member");
        System.out.println("3. Borrow item");
        System.out.println("4. Return item");
        System.out.println("5. Renew loan");
        System.out.println("6. Search item by ID");
        System.out.println("7. View items by status");
        System.out.println("8. Pay outstanding fines");
        System.out.println("9. View all members");
        System.out.println("10. Library report");
        System.out.println("0. Exit");
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println("Choice must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);

                if (value >= 0) {
                    return value;
                }

                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(line);

                if (!Double.isNaN(value) && !Double.isInfinite(value) && value > 0.0) {
                    return value;
                }

                System.out.println("Amount must be a positive finite number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static String readRequiredString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            if (!line.isEmpty()) {
                return line;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static ItemStatus readItemStatus(Scanner scanner) {
        while (true) {
            System.out.print("Enter status (AVAILABLE, ON_LOAN, RESERVED, LOST): ");
            String line = scanner.nextLine().trim().toUpperCase();

            try {
                return ItemStatus.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status. Use one of: AVAILABLE, ON_LOAN, RESERVED, LOST.");
            }
        }
    }

    private static MembershipType readMembershipType(Scanner scanner) {
        while (true) {
            System.out.print("Enter membership type (REGULAR, STUDENT, SENIOR): ");
            String line = scanner.nextLine().trim().toUpperCase();

            try {
                return MembershipType.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Use one of: REGULAR, STUDENT, SENIOR.");
            }
        }
    }
}