package masr;

import java.util.List;
import java.util.function.Predicate;

public final class RestaurantSearchFacade {

    private final RestaurantRepository restaurants;
    private final CustomerRepository customers;

    public RestaurantSearchFacade(RestaurantRepository restaurants, CustomerRepository customers) {
        this.restaurants = restaurants;
        this.customers = customers;
    }

    public List<Restaurant> find(Predicate<Restaurant> condition) {
        if (condition == null) {
            return restaurants.byRating();
        }
        return restaurants.stream().filter(condition).sorted(RestaurantRepository.BY_RATING_THEN_NAME).toList();
    }

    public List<Restaurant> findByRating() {
        return restaurants.byRating();
    }

    public List<Restaurant> browseOpen(Customer customer, RestaurantSearch.SearchCriteria criteria) {
        List<Restaurant> results = find(RestaurantSearch.from(criteria));
        if (customer != null) {
            customer.recordSearch(criteria.describe());
        }
        return results;
    }

    public List<Restaurant> freeTextSearch(Customer customer, String freeText) {
        if (freeText == null || freeText.isBlank()) {
            return List.of();
        }
        if (customer != null) {
            customer.recordSearch(freeText.trim());
        }
        return find(RestaurantSearch.servingCuisineOrName(freeText));
    }

    public List<Restaurant> topRated(int limit) {
        return restaurants.byRating().stream().limit(limit).toList();
    }

    public List<Restaurant> byCuisine(Cuisine cuisine) {
        return find(RestaurantSearch.withCuisine(cuisine));
    }

    public List<Restaurant> byDistrict(District district) {
        return find(RestaurantSearch.inDistrict(district));
    }
}
