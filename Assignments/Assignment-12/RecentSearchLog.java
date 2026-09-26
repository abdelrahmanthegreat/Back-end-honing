package masr;

import java.util.ArrayList;
import java.util.List;

public final class RecentSearchLog {

    private final int capacity;
    private final String[] terms;
    private int size;

    public RecentSearchLog(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.terms = new String[capacity];
    }

    public synchronized void record(String term) {
        if (term == null || term.isBlank()) {
            return;
        }
        String value = term.trim();
        int index = size < capacity ? size : capacity - 1;
        for (int i = index; i > 0; i--) {
            terms[i] = terms[i - 1];
        }
        terms[0] = value;
        if (size < capacity) {
            size++;
        }
    }

    public synchronized List<String> newestFirst() {
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(terms[i]);
        }
        return List.copyOf(result);
    }

    public synchronized int size() {
        return size;
    }

    public synchronized int capacity() {
        return capacity;
    }

    public synchronized void clear() {
        java.util.Arrays.fill(terms, null);
        size = 0;
    }

    public synchronized boolean contains(String term) {
        return term != null && newestFirst().stream().anyMatch(stored -> stored.equals(term));
    }

    @Override
    public synchronized String toString() {
        return newestFirst().toString();
    }
}
