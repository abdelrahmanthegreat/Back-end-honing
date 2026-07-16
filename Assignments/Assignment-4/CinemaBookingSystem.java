import java.util.Scanner;

public class CinemaBookingSystem {
    static char[][] seats;
    static String[] movieNames;
    static int rows;
    static int cols;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        setupCinema();
        while (true) {
            System.out.println("1. Display Seats\n2. Book Seat\n3. Cancel Booking\n4. Show All Movies\n5. Show Available & Booked Seats\n0. Exit");
            System.out.print("Enter your choice: ");
            String choiceStr = sc.nextLine();
            int choice = -1;
            try { choice = Integer.parseInt(choiceStr); } catch (Exception e) { choice = -1; }
            if (choice == 1) displaySeats();
            else if (choice == 2) bookSeat();
            else if (choice == 3) cancelBooking();
            else if (choice == 4) showMovies();
            else if (choice == 5) showStats();
            else if (choice == 0) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void setupCinema() {
        System.out.println("Cinema Ticket Booking System");
        System.out.print("Enter number of rows: ");
        String rInput = sc.nextLine();
        rows = rInput.isEmpty() ? 5 : Integer.parseInt(rInput);
        System.out.print("Enter number of seats per row: ");
        String cInput = sc.nextLine();
        cols = cInput.isEmpty() ? 6 : Integer.parseInt(cInput);
        seats = new char[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                seats[i][j] = 'O';
        System.out.print("Enter number of movies: ");
        String mInput = sc.nextLine();
        int numMovies = mInput.isEmpty() ? 5 : Integer.parseInt(mInput);
        if (numMovies == 5) {
            movieNames = new String[]{"Superman", "Avatar", "Minecraft", "Inside Out", "F1"};
        } else {
            movieNames = new String[numMovies];
            for (int i = 0; i < numMovies; i++) {
                System.out.print("Movie " + (i + 1) + " name: ");
                movieNames[i] = sc.nextLine();
            }
        }
    }

    static void displaySeats() {
        System.out.println("Seat Map (O = Available, X = Booked)");
        System.out.print("  ");
        for (int j = 1; j <= cols; j++) {
            System.out.print(j + " ");
        }
        System.out.println();
        for (int i = 0; i < rows; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < cols; j++) {
                System.out.print(seats[i][j] + " ");
            }
            System.out.println();
        }
        checkAlmostFull();
    }

    static void bookSeat() {
        System.out.print("Enter seat to book (row*column): ");
        String s = sc.nextLine();
        if (s.length() == 2 && Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1))) {
            int r = s.charAt(0) - '0' - 1;
            int c = s.charAt(1) - '0' - 1;
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                if (seats[r][c] == 'X') {
                    System.out.println("Seat is already booked.");
                } else {
                    seats[r][c] = 'X';
                    System.out.println("Seat booked successfully!");
                    checkAlmostFull();
                }
            } else {
                System.out.println("Invalid Seat.");
            }
        } else {
            System.out.println("Invalid Seat.");
        }
    }

    static void cancelBooking() {
        System.out.print("Enter seat to cancel (e.g. 12): ");
        String s = sc.nextLine();
        if (s.length() == 2 && Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1))) {
            int r = s.charAt(0) - '0' - 1;
            int c = s.charAt(1) - '0' - 1;
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                if (seats[r][c] == 'O') {
                    System.out.println("Seat is not booked.");
                } else {
                    seats[r][c] = 'O';
                    System.out.println("Booking cancelled.");
                }
            } else {
                System.out.println("Invalid Seat.");
            }
        } else {
            System.out.println("Invalid Seat.");
        }
    }

    static void showMovies() {
        for (int i = 0; i < movieNames.length; i++) {
            System.out.println((i + 1) + ". " + movieNames[i]);
        }
    }

    static void showStats() {
        int booked = 0;
        int available = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seats[i][j] == 'X') booked++;
                else available++;
            }
        }
        System.out.println("Available: " + available);
        System.out.println("Booked: " + booked);
        checkAlmostFull();
    }

    static void checkAlmostFull() {
        int bookedCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seats[i][j] == 'X') bookedCount++;
            }
        }
        if (bookedCount * 100 / (rows * cols) > 80) {
            System.out.println("Almost Full");
        }
    }
}