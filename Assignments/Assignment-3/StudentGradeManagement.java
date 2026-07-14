import java.util.Scanner;

public class StudentGradeManagement {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        String[] names = new String[5];
        int[][] grades = new int[5][3];
        
        // Input students
        for (int i = 0; i < 5; i++) {
            System.out.println("Student " + (i + 1) + ":");
            System.out.print("Name: ");
            names[i] = sc.nextLine();
            
            for (int j = 0; j < 3; j++) {
                int grade = -1;
                while (grade < 0 || grade > 100) {
                    System.out.print("Subject " + (j + 1) + " grade: ");
                    grade = sc.nextInt();
                    if (grade < 0 || grade > 100) {
                        System.out.println("Invalid grade.");
                    }
                }
                grades[i][j] = grade;
            }
            sc.nextLine();
        }
        
        // Menu
        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Show All Students names");
            System.out.println("2. Show all Students grades");
            System.out.println("3. Search Student by name");
            System.out.println("4. Count Passed Students");
            System.out.println("5. Subject averages");
            System.out.println("6. Highest grades");
            System.out.println("7. Letter grades");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            if (choice == 1) {
                System.out.println("\nStudents:");
                for (int i = 0; i < 5; i++) {
                    System.out.println((i + 1) + ". " + names[i]);
                }
            }
            
            else if (choice == 2) {
                System.out.println("\nGrades:");
                for (int i = 0; i < 5; i++) {
                    System.out.println(names[i] + ":");
                    for (int j = 0; j < 3; j++) {
                        System.out.println("  Subject " + (j + 1) + ": " + grades[i][j]);
                    }
                }
            }
            
            else if (choice == 3) {
                System.out.print("Enter name: ");
                String search = sc.nextLine();
                boolean found = false;
                
                for (int i = 0; i < 5; i++) {
                    if (names[i].equalsIgnoreCase(search)) {
                        found = true;
                        System.out.println("Found: " + names[i]);
                        for (int j = 0; j < 3; j++) {
                            System.out.println("  Subject " + (j + 1) + ": " + grades[i][j]);
                        }
                    }
                }
                if (!found) {
                    System.out.println("Not found");
                }
            }
            
            else if (choice == 4) {
                int count = 0;
                for (int i = 0; i < 5; i++) {
                    int sum = 0;
                    for (int j = 0; j < 3; j++) {
                        sum += grades[i][j];
                    }
                    if (sum / 3 >= 50) {
                        count++;
                    }
                }
                System.out.println("Passed: " + count);
            }
            
            else if (choice == 5) {
                System.out.println("\nAverages:");
                for (int j = 0; j < 3; j++) {
                    int sum = 0;
                    for (int i = 0; i < 5; i++) {
                        sum += grades[i][j];
                    }
                    System.out.println("Subject " + (j + 1) + ": " + (sum / 5.0));
                }
            }
            
            else if (choice == 6) {
                System.out.println("\nHighest:");
                for (int j = 0; j < 3; j++) {
                    int max = grades[0][j];
                    for (int i = 1; i < 5; i++) {
                        if (grades[i][j] > max) {
                            max = grades[i][j];
                        }
                    }
                    System.out.println("Subject " + (j + 1) + ": " + max);
                }
            }
            
            else if (choice == 7) {
                System.out.println("\nLetter Grades:");
                for (int i = 0; i < 5; i++) {
                    System.out.println(names[i] + ":");
                    for (int j = 0; j < 3; j++) {
                        int g = grades[i][j];
                        String letter = "";
                        if (g >= 85) letter = "A";
                        else if (g >= 75) letter = "B";
                        else if (g >= 65) letter = "C";
                        else if (g >= 50) letter = "D";
                        else letter = "F";
                        System.out.println("  Subject " + (j + 1) + ": " + g + " - " + letter);
                    }
                }
            }
            
            else if (choice == 0) {
                exit = true;
                System.out.println("Bye!");
            }
            
            else {
                System.out.println("Invalid choice");
            }
        }
        
        sc.close();
    }
}