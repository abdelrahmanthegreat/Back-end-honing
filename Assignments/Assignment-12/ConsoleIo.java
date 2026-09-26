package masr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.Function;

public final class ConsoleIo {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void print(String text) {
        System.out.print(text);
        System.out.flush();
    }

    public void println() {
        System.out.println();
    }

    public void println(String text) {
        System.out.println(text);
    }

    public void printf(String format, Object... args) {
        System.out.printf(format, args);
        System.out.flush();
    }

    public void heading(String text) {
        println();
        println("--------------------------------------------");
        println(text);
        println("--------------------------------------------");
    }

    public void banner(String title) {
        println();
        println("============================================");
        println(title);
        println("============================================");
    }

    public void success(String message) {
        println("OK  " + message);
    }

    public void info(String message) {
        println("    " + message);
    }

    public void warn(String message) {
        println("!!  " + message);
    }

    public void error(String message) {
        println("XX  " + message);
    }

    public String readLine(String prompt) {
        print(prompt);
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new SessionEndedException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new SessionEndedException();
        }
    }

    public String readOptional(String prompt) {
        return readLine(prompt);
    }

    public String readRequired(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            warn("This field is required.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                warn("'" + line + "' is not a whole number. Please type digits only.");
            }
        }
    }

    public int readIntBetween(String prompt, int minimum, int maximum) {
        while (true) {
            int value = readInt(prompt);
            if (value < minimum || value > maximum) {
                warn("Please choose a number between " + minimum + " and " + maximum + ".");
                continue;
            }
            return value;
        }
    }

    public int readIntOrDefault(String prompt, int fallback) {
        String line = readLine(prompt);
        if (line.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            warn("'" + line + "' is not a whole number. Please type digits only.");
            return readIntOrDefault(prompt, fallback);
        }
    }

    public int readChoice(String prompt, int optionCount) {
        while (true) {
            int value = readIntOrDefault(prompt, 0);
            if (value < 0 || value > optionCount) {
                warn("Please choose a number between 0 and " + optionCount + ".");
                continue;
            }
            return value;
        }
    }

    public BigDecimal readDecimal(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                warn("'" + line + "' is not a valid number, for example 12 or 12.50.");
            }
        }
    }

    public BigDecimal readPositiveDecimal(String prompt) {
        while (true) {
            BigDecimal value = readDecimal(prompt);
            if (value.signum() <= 0) {
                warn("The value must be greater than zero.");
                continue;
            }
            return value;
        }
    }

    public double readRating(String prompt) {
        while (true) {
            BigDecimal value = readDecimal(prompt);
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("5")) > 0) {
                warn("A rating must be between 0.0 and 5.0 inclusive.");
                continue;
            }
            return value.doubleValue();
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String line = readLine(prompt).toLowerCase(Locale.ROOT);
            if (line.equals("y") || line.equals("yes") || line.equals("true") || line.equals("1")) {
                return true;
            }
            if (line.equals("n") || line.equals("no") || line.equals("false") || line.equals("0")) {
                return false;
            }
            warn("Please answer y or n.");
        }
    }

    public String readRequiredEnum(String prompt, Function<String, String> parser, String invalidMessage) {
        while (true) {
            String line = readRequired(prompt);
            String parsed = parser.apply(line);
            if (parsed == null) {
                warn(invalidMessage);
                continue;
            }
            return parsed;
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String line = readRequired(prompt);
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                warn("'" + line + "' is not a date. Use the format yyyy-mm-dd, for example 2026-12-31.");
            }
        }
    }

    public void pause() {
        readLine("Press enter to continue...");
    }

    public static class SessionEndedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public SessionEndedException() {
            super("input stream closed");
        }
    }
}
