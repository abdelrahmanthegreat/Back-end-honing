import java.util.Scanner;

class Student {
    private final int id;
    private final String name;
    private final double[] subjectGrades;

    public Student(int id, String name, double sub1, double sub2) {
        this.id = id;
        this.name = name;
        this.subjectGrades = new double[]{sub1, sub2};
    }

    public int getId() { return id; }
    
    public double getFinalGrade() {
        return (subjectGrades[0] + subjectGrades[1]) / 2.0;
    }

    public String getGradeStatus() {
        double grade = getFinalGrade();
        if (grade >= 90) return "Excellent";
        if (grade >= 75) return "Very Good";
        if (grade >= 60) return "Pass";
        return "Fail";
    }

    public void displayInfo() {
        System.out.printf("| %-6d | %-15s | %-8.1f | %-8.1f | %-10.2f | %-10s |%n",
                id, name, subjectGrades[0], subjectGrades[1], getFinalGrade(), getGradeStatus());
    }
}

public class StudentManagementSystem {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter the number of students (N): ");
        int n = readIntInRange(scanner, 1, 1000);
        Student[] students = new Student[n];

        System.out.println("\nEnter Student Details");
        for (int i = 0; i < n; i++) {
            System.out.println("\nStudent " + (i + 1) + " ID:");
            int id = readUniqueId(scanner, students, i);
            String name = readNonEmptyString(scanner, "Enter Name: ");
            double sub1 = readGrade(scanner, "Subject 1");
            double sub2 = readGrade(scanner, "Subject 2");
            
            students[i] = new Student(id, name, sub1, sub2);
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInRange(scanner, 0, 6);

            switch (choice) {
                case 1 -> displayStudents(students);
                case 2 -> calculateAverage(students);
                case 3 -> findHighestGrade(students);
                case 4 -> searchById(students, scanner);
                case 5 -> countPassFail(students);
                case 6 -> sortAndDisplay(students);
                case 0 -> {
                    System.out.println("Exiting system.");
                    running = false;
                }
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\nMAIN MENU");
        System.out.println("1. Display Students");
        System.out.println("2. Calculate Average Grade");
        System.out.println("3. Find Highest Grade");
        System.out.println("4. Search Student by ID");
        System.out.println("5. Count Passed & Failed Students");
        System.out.println("6. Sort Students by Grade");
        System.out.println("0. Exit");
    }

    private static void displayStudents(Student[] students) {
        System.out.printf("| %-6s | %-15s | %-8s | %-8s | %-10s | %-10s |%n", "ID", "Name", "Sub 1", "Sub 2", "Final", "Status");
        for (Student s : students) {
            if (s != null) s.displayInfo();
        }
    }

    private static void calculateAverage(Student[] students) {
        double sum = 0;
        int count = 0;
        for (Student s : students) {
            if (s != null) {
                sum += s.getFinalGrade();
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No students to calculate average.");
            return;
        }
        System.out.printf("Class Average Grade: %.2f%n", sum / count);
    }

    private static void findHighestGrade(Student[] students) {
        Student highest = null;
        for (Student s : students) {
            if (s != null && (highest == null || s.getFinalGrade() > highest.getFinalGrade())) {
                highest = s;
            }
        }
        if (highest == null) {
            System.out.println("No students in the system.");
            return;
        }
        System.out.println("Highest Grade Student");
        highest.displayInfo();
    }

    private static void searchById(Student[] students, Scanner scanner) {
        int searchId = readIntInRange(scanner, 1, Integer.MAX_VALUE);
        for (Student s : students) {
            if (s != null && s.getId() == searchId) {
                System.out.println("Search Result");
                s.displayInfo();
                return;
            }
        }
        System.out.println("Student not found.");
    }

    private static void countPassFail(Student[] students) {
        int passCount = 0, failCount = 0;
        for (Student s : students) {
            if (s != null) {
                if (s.getFinalGrade() >= 60) passCount++;
                else failCount++;
            }
        }
        System.out.println("Passed: " + passCount + " | Failed: " + failCount);
    }

    private static void sortAndDisplay(Student[] students) {
        for (int i = 0; i < students.length - 1; i++) {
            for (int j = i + 1; j < students.length; j++) {
                if (students[i] != null && students[j] != null && 
                    students[j].getFinalGrade() > students[i].getFinalGrade()) {
                    Student temp = students[i];
                    students[i] = students[j];
                    students[j] = temp;
                }
            }
        }
        System.out.println("Sorted by Grade (Highest to Lowest)");
        displayStudents(students);
    }

    private static int readIntInRange(Scanner sc, int min, int max) {
        while (true) {
            if (sc.hasNextInt()) {
                int val = sc.nextInt();
                sc.nextLine(); 
                if (val >= min && val <= max) return val;
                System.out.printf("Error: Value must be between %d and %d.%n", min, max);
            } else {
                System.out.println("Error: Invalid input. Please enter a numeric value.");
                sc.next(); 
            }
        }
    }

    private static int readUniqueId(Scanner sc, Student[] students, int currentIndex) {
        while (true) {
            int id = readIntInRange(sc, 1, Integer.MAX_VALUE);
            boolean isDuplicate = false;
            for (int i = 0; i < currentIndex; i++) {
                if (students[i].getId() == id) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) return id;
            System.out.println("Error: ID already exists. Please enter a unique ID.");
        }
    }

    private static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Error: Input cannot be empty.");
        }
    }

    private static double readGrade(Scanner sc, String subjectName) {
        while (true) {
            System.out.printf("Enter %s grade (0-100): ", subjectName);
            if (sc.hasNextDouble()) {
                double grade = sc.nextDouble();
                sc.nextLine(); 
                if (grade >= 0 && grade <= 100) return grade;
                System.out.println("Error: Grade must be between 0 and 100.");
            } else {
                System.out.println("Error: Invalid input. Please enter a numeric grade.");
                sc.next(); 
            }
        }
    }
}