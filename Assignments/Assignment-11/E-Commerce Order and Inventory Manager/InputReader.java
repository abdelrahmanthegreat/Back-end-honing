import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputReader {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Please try again.");
            }
        }
    }

    public int readInt(String prompt, int min) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min) return value;
            System.out.println("Value must be at least " + min + ".");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt, min);
            if (value <= max) return value;
            System.out.println("Value must be between " + min + " and " + max + ".");
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                double value = Double.parseDouble(line);

                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("Invalid number.");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    public double readDouble(String prompt, double min) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= min) return value;
            System.out.println("Value must be at least " + min + ".");
        }
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) return line;
            System.out.println("Input cannot be empty.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);

        try {
            String line = reader.readLine();
            if (line == null) throw new IllegalStateException("Input ended unexpectedly.");
            return line.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read input.", e);
        }
    }
}