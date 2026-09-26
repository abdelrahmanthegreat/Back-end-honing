package masr;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private final AtomicInteger counter = new AtomicInteger();

    public IdGenerator() {
        this(0);
    }

    public IdGenerator(int start) {
        counter.set(start);
    }

    public String next(String prefix) {
        return String.format("%s-%05d", prefix, counter.incrementAndGet());
    }

    public int peek() {
        return counter.get();
    }
}
