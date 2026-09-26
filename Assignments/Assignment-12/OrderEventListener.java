package masr;

@FunctionalInterface
public interface OrderEventListener {

    void onOrderEvent(OrderEvent event);

    default String name() {
        return getClass().getSimpleName();
    }
}
