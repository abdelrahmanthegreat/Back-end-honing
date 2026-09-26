package masr;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class DispatchQueue {

    private final PriorityQueue<Entry> queue = new PriorityQueue<>();
    private final AtomicLong sequence = new AtomicLong();

    synchronized public void offer(Order order, LoyaltyTier priorityTier, LocalDateTime readyAt) {
        queue.add(new Entry(order, priorityTier, readyAt == null ? LocalDateTime.now() : readyAt, sequence.getAndIncrement()));
    }

    synchronized public Optional<Order> pollNext() {
        while (!queue.isEmpty()) {
            Entry entry = queue.poll();
            if (entry.order().status() == OrderStatus.READY) {
                return Optional.of(entry.order());
            }
        }
        return Optional.empty();
    }

    synchronized public Optional<Order> peekNext() {
        return queue.peek() == null ? Optional.empty() : Optional.of(queue.peek().order());
    }

    synchronized public boolean remove(String orderId) {
        return queue.removeIf(entry -> entry.order().id().equals(orderId));
    }

    synchronized public List<Order> pending() {
        List<Entry> entries = new ArrayList<>(queue);
        entries.sort(Entry.ORDER);
        return entries.stream().map(Entry::order).toList();
    }

    synchronized public List<String> pendingLines() {
        List<Entry> entries = new ArrayList<>(queue);
        entries.sort(Entry.ORDER);
        return entries.stream()
                .map(entry -> entry.order().id() + " | " + entry.priorityTier().label()
                        + " | ready since " + entry.readyAt() + " | " + entry.order().total())
                .toList();
    }

    synchronized public int size() {
        return queue.size();
    }

    synchronized public boolean isEmpty() {
        return queue.isEmpty();
    }

    synchronized public void clear() {
        queue.clear();
    }

    public record Entry(Order order, LoyaltyTier priorityTier, LocalDateTime readyAt, long sequence)
            implements Comparable<Entry> {

        public static final Comparator<Entry> ORDER = Comparator
                .comparingInt((Entry entry) -> entry.priorityTier().dispatchPriority())
                .thenComparing(Entry::readyAt)
                .thenComparingLong(Entry::sequence);

        @Override
        public int compareTo(Entry other) {
            return ORDER.compare(this, other);
        }
    }
}
