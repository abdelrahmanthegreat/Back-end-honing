package masr;

public final class RiderDashboardUpdater implements OrderEventListener {

    private final RiderRepository riders;

    public RiderDashboardUpdater(RiderRepository riders) {
        this.riders = riders;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        Order order = event.order();
        riders.updateDashboard(order.riderId(), order);
    }

    @Override
    public String name() {
        return "RiderDashboardUpdater";
    }
}
