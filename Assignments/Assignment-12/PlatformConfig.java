package masr;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class PlatformConfig {

    private static final String RESOURCE_NAME = "config.properties";
    private static final String OVERRIDE_ENV = "MASR_CONFIG";

    private static final class Holder {
        private static final PlatformConfig INSTANCE = load();
    }

    private final Map<String, String> raw;
    private final Money baseDeliveryFee;
    private final Money perExtraKmFee;
    private final int includedKilometres;
    private final BigDecimal serviceFeeRate;
    private final int recentSearchCapacity;
    private final int notificationHistoryCapacity;
    private final int riderSearchRadiusKm;
    private final Path restaurantDataFile;
    private final Path auditLogFile;
    private final Path reportsDirectory;

    private PlatformConfig(Map<String, String> values) {
        this.raw = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        this.baseDeliveryFee = money("delivery.baseFee", "15.00");
        this.perExtraKmFee = money("delivery.perExtraKmFee", "3.00");
        this.includedKilometres = integer("delivery.includedKm", 3);
        this.serviceFeeRate = decimal("fees.serviceRate", "0.10");
        this.recentSearchCapacity = integer("search.recentCapacity", 5);
        this.notificationHistoryCapacity = integer("notify.historyCapacity", 20);
        this.riderSearchRadiusKm = integer("dispatch.searchRadiusKm", 15);
        this.restaurantDataFile = path("data.restaurantFile", "data/restaurants.csv");
        this.auditLogFile = path("data.auditLogFile", "data/audit.log");
        this.reportsDirectory = path("data.reportsDir", "data/reports");
    }

    public static PlatformConfig get() {
        return Holder.INSTANCE;
    }

    public static PlatformConfig of(Map<String, String> values) {
        return new PlatformConfig(values == null ? Map.of() : values);
    }

    public static PlatformConfig defaults() {
        return of(Map.of());
    }

    static PlatformConfig load() {
        return new PlatformConfig(readSources());
    }

    private static Map<String, String> readSources() {
        Map<String, String> values = new LinkedHashMap<>();
        values.putAll(readClasspath());
        values.putAll(readOverridePath());
        values.putAll(readEnvironment());
        return values;
    }

    private static Map<String, String> readClasspath() {
        Properties properties = new Properties();
        try (InputStream in = PlatformConfig.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        Map<String, String> values = new LinkedHashMap<>();
        properties.stringPropertyNames().forEach(key -> values.put(key, properties.getProperty(key)));
        return values;
    }

    private static Map<String, String> readOverridePath() {
        String override = System.getenv(OVERRIDE_ENV);
        if (override == null || override.isBlank()) {
            override = System.getProperty(OVERRIDE_ENV.toLowerCase(java.util.Locale.ROOT));
        }
        if (override == null || override.isBlank()) {
            return Map.of();
        }
        Path path = Path.of(override);
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Configuration file " + path + " exists but could not be read", e);
        }
        Map<String, String> values = new LinkedHashMap<>();
        properties.stringPropertyNames().forEach(key -> values.put(key, properties.getProperty(key)));
        return values;
    }

    private static Map<String, String> readEnvironment() {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("masr.")) {
                values.put(key, System.getProperty(key));
            }
        }
        return values;
    }

    public Map<String, String> rawValues() {
        return raw;
    }

    public String value(String key, String fallback) {
        String found = raw.get(key);
        return found == null || found.isBlank() ? fallback : found.trim();
    }

    public Money baseDeliveryFee() {
        return baseDeliveryFee;
    }

    public Money perExtraKmFee() {
        return perExtraKmFee;
    }

    public int includedKilometres() {
        return includedKilometres;
    }

    public BigDecimal serviceFeeRate() {
        return serviceFeeRate;
    }

    public int recentSearchCapacity() {
        return recentSearchCapacity;
    }

    public int notificationHistoryCapacity() {
        return notificationHistoryCapacity;
    }

    public int riderSearchRadiusKm() {
        return riderSearchRadiusKm;
    }

    public Path restaurantDataFile() {
        return restaurantDataFile;
    }

    public Path auditLogFile() {
        return auditLogFile;
    }

    public Path reportsDirectory() {
        return reportsDirectory;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("base delivery fee      : ").append(baseDeliveryFee).append('\n');
        sb.append("fee per extra km       : ").append(perExtraKmFee).append('\n');
        sb.append("included km per order  : ").append(includedKilometres).append('\n');
        sb.append("service fee rate       : ").append(serviceFeeRate).append('\n');
        sb.append("recent search capacity : ").append(recentSearchCapacity).append('\n');
        sb.append("rider search radius    : ").append(riderSearchRadiusKm).append(" km\n");
        sb.append("restaurant data file   : ").append(restaurantDataFile).append('\n');
        sb.append("audit log file         : ").append(auditLogFile).append('\n');
        sb.append("reports directory      : ").append(reportsDirectory).append('\n');
        return sb.toString();
    }

    private Money money(String key, String fallback) {
        return Money.ofEgp(decimal(key, fallback));
    }

    private BigDecimal decimal(String key, String fallback) {
        return new BigDecimal(value(key, fallback));
    }

    private int integer(String key, int fallback) {
        return Integer.parseInt(value(key, Integer.toString(fallback)).trim());
    }

    private Path path(String key, String fallback) {
        return Path.of(value(key, fallback));
    }
}
