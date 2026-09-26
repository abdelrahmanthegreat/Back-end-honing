package masr;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Repository<T> {

    private final String label;
    private final Function<T, String> idExtractor;
    private final Map<String, T> byId = new LinkedHashMap<>();

    protected Repository(String label, Function<T, String> idExtractor) {
        this.label = label;
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor must not be null");
    }

    public synchronized T add(T entity) throws ValidationException {
        String id = idExtractor.apply(entity);
        if (id == null || id.isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a " + label + " id is required");
        }
        if (byId.containsKey(id)) {
            throw new ValidationException(ValidationException.DUPLICATE_ID,
                    label + " id '" + id + "' is already taken");
        }
        byId.put(id, entity);
        return entity;
    }

    public synchronized Optional<T> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id.trim()));
    }

    public T require(String id) throws ValidationException {
        return find(id).orElseThrow(() -> new ValidationException(ValidationException.NOT_FOUND,
                "no " + label + " is registered with id '" + id + "'"));
    }

    public synchronized boolean remove(String id) {
        return id != null && byId.remove(id.trim()) != null;
    }

    public synchronized List<T> all() {
        return List.copyOf(byId.values());
    }

    public synchronized Collection<T> values() {
        return java.util.Collections.unmodifiableCollection(new java.util.ArrayList<>(byId.values()));
    }

    public synchronized Stream<T> stream() {
        return List.copyOf(byId.values()).stream();
    }

    public synchronized List<T> findAll(Predicate<T> filter) {
        return List.copyOf(byId.values()).stream().filter(filter).toList();
    }

    public synchronized boolean exists(String id) {
        return id != null && byId.containsKey(id.trim());
    }

    public synchronized int size() {
        return byId.size();
    }

    public synchronized void clear() {
        byId.clear();
    }

    public String label() {
        return label;
    }
}
