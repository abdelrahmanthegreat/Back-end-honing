package masr;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RestaurantRepository extends Repository<Restaurant> {

    public RestaurantRepository() {
        super("restaurant", Restaurant::id);
    }

    public static final Comparator<Restaurant> BY_RATING_THEN_NAME =
            Comparator.comparingDouble(Restaurant::rating).reversed()
                    .thenComparing(Restaurant::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Restaurant::id);

    public List<Restaurant> byRating() {
        return stream().sorted(BY_RATING_THEN_NAME).toList();
    }

    public List<Restaurant> openOnly() {
        return stream().filter(Restaurant::isOpen).toList();
    }

    public List<Restaurant> byDistrict(District district) {
        return findAll(restaurant -> restaurant.district() == district);
    }

    public List<Restaurant> byCuisine(Cuisine cuisine) {
        return findAll(restaurant -> restaurant.cuisines().contains(cuisine));
    }

    public Set<Cuisine> distinctCuisines() {
        return stream().flatMap(restaurant -> restaurant.cuisines().stream())
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Cuisine.class)));
    }
}
