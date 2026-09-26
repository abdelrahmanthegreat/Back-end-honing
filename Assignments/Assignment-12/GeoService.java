package masr;

import java.util.EnumMap;
import java.util.Map;

public final class GeoService {

    private static final int FALLBACK_KM = 8;

    private static final Map<District, Map<District, Integer>> DEFAULTS = buildDefaults();

    private final Map<District, Map<District, Integer>> table;

    public GeoService() {
        this(DEFAULTS);
    }

    public GeoService(Map<District, Map<District, Integer>> table) {
        this.table = copy(table);
    }

    public static GeoService fromSpec(String spec) {
        if (spec == null || spec.isBlank()) {
            return new GeoService();
        }
        Map<District, Map<District, Integer>> overrides = new EnumMap<>(District.class);
        for (String pair : spec.split(",")) {
            String[] parts = pair.split("=");
            if (parts.length != 2) {
                continue;
            }
            String[] endpoints = parts[0].trim().split("-");
            if (endpoints.length != 2) {
                continue;
            }
            District from = District.parse(endpoints[0]);
            District to = District.parse(endpoints[1]);
            int km = Integer.parseInt(parts[1].trim());
            if (from != null && to != null) {
                overrides.computeIfAbsent(from, key -> new EnumMap<>(District.class)).put(to, km);
                overrides.computeIfAbsent(to, key -> new EnumMap<>(District.class)).put(from, km);
            }
        }
        if (overrides.isEmpty()) {
            return new GeoService();
        }
        Map<District, Map<District, Integer>> merged = new EnumMap<>(District.class);
        DEFAULTS.forEach((district, row) -> {
            Map<District, Integer> copyRow = new EnumMap<>(District.class);
            copyRow.putAll(row);
            merged.put(district, copyRow);
        });
        overrides.forEach((district, row) -> merged.computeIfAbsent(district, key -> new EnumMap<>(District.class)).putAll(row));
        return new GeoService(merged);
    }

    public int distanceKm(District from, District to) {
        if (from == null || to == null) {
            return FALLBACK_KM;
        }
        if (from == to) {
            return 0;
        }
        Map<District, Integer> row = table.get(from);
        Integer km = row == null ? null : row.get(to);
        if (km == null) {
            km = table.getOrDefault(to, Map.of()).get(from);
        }
        return km == null ? FALLBACK_KM : km;
    }

    private static Map<District, Map<District, Integer>> copy(Map<District, Map<District, Integer>> source) {
        Map<District, Map<District, Integer>> result = new EnumMap<>(District.class);
        source.forEach((district, row) -> {
            Map<District, Integer> copyRow = new EnumMap<>(District.class);
            copyRow.putAll(row);
            result.put(district, Map.copyOf(copyRow));
        });
        return Map.copyOf(result);
    }

    private static Map<District, Map<District, Integer>> buildDefaults() {
        Map<District, Map<District, Integer>> table = new EnumMap<>(District.class);
        put(table, District.MAADI, 0, 12, 18, 16, 12, 10, 7, 20);
        put(table, District.DOKKI, 12, 0, 14, 10, 5, 3, 9, 15);
        put(table, District.NASR_CITY, 18, 14, 0, 6, 10, 13, 25, 8);
        put(table, District.HELIOPOLIS, 16, 10, 6, 0, 9, 9, 28, 7);
        put(table, District.FAISAL, 12, 5, 10, 9, 0, 8, 14, 11);
        put(table, District.ZAMALEK, 10, 3, 13, 9, 8, 0, 11, 16);
        put(table, District.GIZA, 7, 9, 25, 28, 14, 11, 0, 27);
        put(table, District.AIN_SHAMS, 20, 15, 8, 7, 11, 16, 27, 0);
        return table;
    }

    private static void put(Map<District, Map<District, Integer>> table, District row, int... values) {
        District[] districts = District.values();
        Map<District, Integer> cells = new EnumMap<>(District.class);
        for (int i = 0; i < districts.length; i++) {
            cells.put(districts[i], values[i]);
        }
        table.put(row, cells);
    }
}
