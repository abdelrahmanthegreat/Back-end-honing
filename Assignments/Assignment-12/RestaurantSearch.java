package masr;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public final class RestaurantSearch {

    private RestaurantSearch() {
    }

    public static Predicate<Restaurant> inDistrict(District district) {
        return restaurant -> district == null || restaurant.district() == district;
    }

    public static Predicate<Restaurant> withCuisine(Cuisine cuisine) {
        return restaurant -> cuisine == null || restaurant.cuisines().contains(cuisine);
    }

    public static Predicate<Restaurant> minRating(double minimum) {
        return restaurant -> restaurant.rating() >= minimum;
    }

    public static Predicate<Restaurant> maxItemPrice(Money ceiling) {
        return restaurant -> ceiling == null || restaurant.menuStream().allMatch(item -> item.unitPrice().compareTo(ceiling) <= 0);
    }

    public static Predicate<Restaurant> hasItemAtMost(Money ceiling) {
        return restaurant -> ceiling == null || restaurant.menuStream().anyMatch(item -> item.unitPrice().compareTo(ceiling) <= 0);
    }

    public static Predicate<Restaurant> openOnly() {
        return Restaurant::isOpen;
    }

    public static Predicate<Restaurant> named(String freeText) {
        return restaurant -> freeText == null || freeText.isBlank()
                || restaurant.name().toLowerCase(Locale.ROOT).contains(freeText.trim().toLowerCase(Locale.ROOT));
    }

    public static Predicate<Restaurant> servingCuisineOrName(String freeText) {
        return restaurant -> {
            if (freeText == null || freeText.isBlank()) {
                return true;
            }
            String needle = freeText.trim().toLowerCase(Locale.ROOT);
            return restaurant.name().toLowerCase(Locale.ROOT).contains(needle)
                    || restaurant.cuisines().stream().anyMatch(cuisine -> cuisine.matches(freeText))
                    || restaurant.district().label().toLowerCase(Locale.ROOT).contains(needle);
        };
    }

    public static Predicate<Restaurant> servingItem(String freeText) {
        return restaurant -> freeText == null || freeText.isBlank()
                || restaurant.menuStream().anyMatch(item -> item.name().toLowerCase(Locale.ROOT).contains(freeText.trim().toLowerCase(Locale.ROOT))
                || item.type().name().toLowerCase(Locale.ROOT).contains(freeText.trim().toLowerCase(Locale.ROOT)));
    }

    @SafeVarargs
    public static <T> Predicate<T> allOf(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates, "predicates must not be null");
        return target -> {
            for (Predicate<T> predicate : predicates) {
                if (predicate != null && !predicate.test(target)) {
                    return false;
                }
            }
            return true;
        };
    }

    @SafeVarargs
    public static <T> Predicate<T> anyOf(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates, "predicates must not be null");
        return target -> {
            boolean any = false;
            for (Predicate<T> predicate : predicates) {
                if (predicate != null) {
                    any = any || predicate.test(target);
                }
            }
            return any;
        };
    }

    public static <T> Predicate<T> negate(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static Predicate<Restaurant> from(SearchCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");
        return criteria.toPredicate();
    }

    public record SearchCriteria(District district,
                                 Cuisine cuisine,
                                 BigDecimal minimumRating,
                                 Money maximumPrice,
                                 boolean openOnly,
                                 String text) {

        public static SearchCriteria none() {
            return new SearchCriteria(null, null, null, null, false, null);
        }

        public Predicate<Restaurant> toPredicate() {
            Predicate<Restaurant> predicate = restaurant -> true;
            if (openOnly) {
                predicate = Restaurant::isOpen;
            }
            if (district != null) {
                predicate = predicate.and(inDistrict(district));
            }
            if (cuisine != null) {
                predicate = predicate.and(withCuisine(cuisine));
            }
            if (minimumRating != null) {
                predicate = predicate.and(minRating(minimumRating.doubleValue()));
            }
            if (maximumPrice != null) {
                predicate = predicate.and(hasItemAtMost(maximumPrice));
            }
            if (text != null && !text.isBlank()) {
                predicate = predicate.and(servingCuisineOrName(text));
            }
            return predicate;
        }

        public String describe() {
            StringBuilder sb = new StringBuilder();
            if (district != null) {
                sb.append("district=").append(district.label());
            }
            if (cuisine != null) {
                sb.append(sb.length() > 0 ? ", " : "").append("cuisine=").append(cuisine.label());
            }
            if (minimumRating != null) {
                sb.append(sb.length() > 0 ? ", " : "").append("minRating=").append(minimumRating.toPlainString());
            }
            if (maximumPrice != null) {
                sb.append(sb.length() > 0 ? ", " : "").append("maxPrice=").append(maximumPrice.format()).append(" EGP");
            }
            if (openOnly) {
                sb.append(sb.length() > 0 ? ", " : "").append("open only");
            }
            if (text != null && !text.isBlank()) {
                sb.append(sb.length() > 0 ? ", " : "").append("text='").append(text.trim()).append('\'');
            }
            return sb.length() == 0 ? "no filters" : sb.toString();
        }
    }
}
