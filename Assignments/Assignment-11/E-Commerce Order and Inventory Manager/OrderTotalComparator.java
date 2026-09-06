import java.util.Comparator;

public class OrderTotalComparator implements Comparator<Order> {
    @Override
    public int compare(Order first, Order second) {
        int result = Double.compare(first.getTotal(), second.getTotal());
        if (result != 0) return result;
        return Integer.compare(first.getOrderId(), second.getOrderId());
    }
}