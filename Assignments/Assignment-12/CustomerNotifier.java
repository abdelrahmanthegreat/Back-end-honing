package masr;

import java.util.List;

public final class CustomerNotifier implements OrderEventListener {

    private final CustomerRepository customers;

    public CustomerNotifier(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        Order order = event.order();
        customers.notifyCustomer(order.customer().id(), messageFor(event));
    }

    private String messageFor(OrderEvent event) {
        Order order = event.order();
        return switch (event.newStatus()) {
            case PLACED -> "Order " + order.id() + " was accepted by the kitchen, total " + order.total();
            case ACCEPTED -> "Restaurant " + order.restaurant().name() + " accepted order " + order.id();
            case PREPARING -> "Order " + order.id() + " is being prepared";
            case READY -> "Order " + order.id() + " is ready and waiting for a rider";
            case ASSIGNED -> "A rider was assigned to order " + order.id();
            case OUT_FOR_DELIVERY -> "Order " + order.id() + " is on the way to " + order.address().shortLine();
            case DELIVERED -> "Order " + order.id() + " was delivered, total paid " + order.total();
            case CANCELLED -> "Order " + order.id() + " was cancelled"
                    + (event.detail() == null ? "" : ": " + event.detail());
        };
    }

    @Override
    public String name() {
        return "CustomerNotifier";
    }

    public List<String> notificationsFor(String customerId) throws ValidationException {
        return customers.require(customerId).notifications();
    }
}
