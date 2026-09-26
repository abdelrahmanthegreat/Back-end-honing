package masr;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuditLog {

    private final List<Entry> entries = new CopyOnWriteArrayList<>();
    private final int capacity;

    public AuditLog() {
        this(2000);
    }

    public AuditLog(int capacity) {
        this.capacity = capacity;
    }

    public void record(String actor, OrderEvent event) {
        entries.add(new Entry(LocalDateTime.now(), actor, event.orderId(),
                event.previousStatus() == null ? "CREATED" : event.previousStatus().name(),
                event.newStatus().name(), event.detail()));
        while (entries.size() > capacity) {
            entries.remove(0);
        }
    }

    public void record(String actor, String message) {
        entries.add(new Entry(LocalDateTime.now(), actor, null, null, null, message));
        while (entries.size() > capacity) {
            entries.remove(0);
        }
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public List<Entry> entriesForOrder(String orderId) {
        return entries.stream().filter(entry -> entry.orderId() != null && entry.orderId().equals(orderId)).toList();
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : entries) {
            sb.append(entry).append('\n');
        }
        return sb.toString();
    }

    public record Entry(LocalDateTime at, String actor, String orderId, String from, String to, String detail) {

        @Override
        public String toString() {
            return at + " | " + String.format("%-12s", actor == null ? "system" : actor)
                    + " | " + (orderId == null ? "-" : orderId)
                    + " | " + (from == null ? "-" : from) + " -> " + (to == null ? "-" : to)
                    + " | " + (detail == null ? "" : detail);
        }
    }
}
