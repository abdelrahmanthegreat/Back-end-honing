import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputReader {
    private final Scanner scanner;
    private boolean eof;

    public InputReader() {
        scanner = new Scanner(System.in);
        eof = false;
    }

    public boolean isEof() {
        return eof;
    }

    public String readLine(String prompt) {
        if (eof) {
            return null;
        }

        System.out.print(prompt);

        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            eof = true;
            return null;
        }
    }

    public Integer readInt(String prompt) {
        String line = readLine(prompt);
        if (line == null || line.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double readDouble(String prompt) {
        String line = readLine(prompt);
        if (line == null || line.isEmpty()) {
            return null;
        }

        try {
            double value = Double.parseDouble(line);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}