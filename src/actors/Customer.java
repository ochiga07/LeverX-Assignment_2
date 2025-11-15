package actors;

import model.Order;
import model.Product;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Customer implements Runnable {
    private final String name;
    private final BlockingQueue<Order> orderQueue;
    private final List<Product> catalog;
    private final Random random;
    private final int ordersToCreate;

    public Customer(String name, BlockingQueue<Order> orderQueue, List<Product> catalog, int ordersToCreate) {
        this.name = name;
        this.orderQueue = orderQueue;
        this.catalog = catalog;
        this.ordersToCreate = ordersToCreate;
        this.random = new Random();
    }

    @Override
    public void run() {
        for(int i = 0;i < ordersToCreate; i++){
            try {
                Order order = createRandomOrder();
                orderQueue.put(order);
                System.out.printf("[CREATED] %s created %s%n", name, order);
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Order createRandomOrder() {
        Order order = new Order(name);
        int itemCount = random.nextInt(3) + 1;

        for (int i = 0; i < itemCount; i++) {
            Product product = catalog.get(random.nextInt(catalog.size()));
            int quantity = random.nextInt(3) + 1;
            order.addItem(product, quantity);
        }

        return order;
    }
}
