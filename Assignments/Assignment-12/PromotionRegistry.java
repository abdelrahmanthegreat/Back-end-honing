package masr;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PromotionRegistry {

    private final Map<String, Promotion> byCode = new LinkedHashMap<>();

    public Promotion register(Promotion promotion) throws PromotionException {
        String code = Promotion.normaliseCode(promotion.code());
        if (byCode.containsKey(code)) {
            throw new PromotionException(PromotionException.DUPLICATE_CODE,
                    "promotion code " + code + " is already registered");
        }
        byCode.put(code, promotion);
        return promotion;
    }

    public boolean remove(String code) {
        return code != null && byCode.remove(Promotion.normaliseCode(code)) != null;
    }

    public Optional<Promotion> find(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCode.get(code.trim().toUpperCase(Locale.ROOT)));
    }

    public Promotion require(String code) throws PromotionException {
        return find(code).orElseThrow(() -> new PromotionException(PromotionException.UNKNOWN_CODE,
                "no promotion is registered with code '" + code + "'"));
    }

    public List<Promotion> all() {
        return List.copyOf(byCode.values());
    }

    public Collection<String> codes() {
        return List.copyOf(byCode.keySet());
    }

    public int size() {
        return byCode.size();
    }

    public void clear() {
        byCode.clear();
    }
}
