import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Restaurant {
    private ArrayList<MenuItem> menu;
    private LinkedList<Order> kitchenQueue;
    private HashMap<Integer, Order> orders;
    private LinkedHashMap<Integer, Order> completedOrders;

    public Restaurant() {
        menu = new ArrayList<>();
        kitchenQueue = new LinkedList<>();
        orders = new HashMap<>();
        completedOrders = new LinkedHashMap<>();
    }

    public boolean addMenuItem(int id, String name, double price, String category) {
        if (findMenuItemById(id) != null) {
            System.out.println("Menu item with ID " + id + " already exists.");
            return false;
        }
        menu.add(new MenuItem(id, name, price, category));
        System.out.println("Menu item added successfully.");
        return true;
    }

    public boolean removeMenuItem(int id) {
        MenuItem item = findMenuItemById(id);
        if (item == null) {
            System.out.println("Menu item with ID " + id + " not found.");
            return false;
        }
        menu.remove(item);
        System.out.println("Menu item removed successfully.");
        return true;
    }

    public void displayMenu() {
        if (menu.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }
        System.out.println("--- Menu ---");
        for (MenuItem item : menu) {
            System.out.println(item);
        }
    }

    public void searchMenuItem(int id) {
        MenuItem item = findMenuItemById(id);
        if (item == null) {
            System.out.println("Menu item with ID " + id + " not found.");
        } else {
            System.out.println("Found: " + item);
        }
    }

    public boolean createOrder(int orderId, String customerName) {
        if (orders.containsKey(orderId)) {
            System.out.println("Order with ID " + orderId + " already exists.");
            return false;
        }
        Order order = new Order(orderId, customerName);
        orders.put(orderId, order);
        System.out.println("Order created successfully (PENDING).");
        return true;
    }

    public void addItemToOrder(int orderId, int menuItemId, int quantity) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("Cannot add items to a " + order.getStatus() + " order.");
            return;
        }
        MenuItem menuItem = findMenuItemById(menuItemId);
        if (menuItem == null) {
            System.out.println("Menu item with ID " + menuItemId + " not found.");
            return;
        }
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }
        order.addItem(new OrderItem(menuItem, quantity));
        System.out.println("Item added to order.");
    }

    public void removeItemFromOrder(int orderId, int menuItemId) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("Cannot remove items from a " + order.getStatus() + " order.");
            return;
        }
        if (order.removeItem(menuItemId)) {
            System.out.println("Item removed from order.");
        } else {
            System.out.println("Item with ID " + menuItemId + " not found in order.");
        }
    }

    public void displayOrder(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
            return;
        }
        order.displayOrder();
    }

    public void addOrderToKitchenQueue(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            System.out.println("Order is not PENDING; cannot add to kitchen queue.");
            return;
        }
        kitchenQueue.add(order);
        order.setStatus(OrderStatus.IN_KITCHEN);
        System.out.println("Order added to kitchen queue and status changed to IN_KITCHEN.");
    }

    public void processNextOrder() {
        if (kitchenQueue.isEmpty()) {
            System.out.println("Kitchen queue is empty.");
            return;
        }
        Order order = kitchenQueue.poll();
        order.setStatus(OrderStatus.COMPLETED);
        completedOrders.put(order.getOrderId(), order);
        System.out.println("Order " + order.getOrderId() + " processed and marked COMPLETED.");
    }

    public void searchOrder(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
        } else {
            System.out.println("Order found:");
            order.displayOrder();
        }
    }

    public void checkOrderStatus(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            System.out.println("Order with ID " + orderId + " not found.");
        } else {
            System.out.println("Order " + orderId + " status: " + order.getStatus());
        }
    }

    public void displayCompletedOrders() {
        if (completedOrders.isEmpty()) {
            System.out.println("No completed orders yet.");
            return;
        }
        System.out.println("--- Completed Orders (in completion order) ---");
        for (Order order : completedOrders.values()) {
            order.displayOrder();
            System.out.println("--------------------");
        }
    }

    private MenuItem findMenuItemById(int id) {
        for (MenuItem item : menu) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}