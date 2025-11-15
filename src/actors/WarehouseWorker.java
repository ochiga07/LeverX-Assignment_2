package actors;

import model.Order;
import model.OrderResult;
import warehouse.Warehouse;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WarehouseWorker implements Runnable {
    private final int workerId;
    private final BlockingQueue<Order> orderQueue;
    private final Warehouse warehouse;
    private final List<OrderResult> results;
    private final Order poisonPill;

    public WarehouseWorker(int workerId, BlockingQueue<Order> orderQueue,
                           Warehouse warehouse, List<OrderResult> results, Order poisonPill) {
        this.workerId = workerId;
        this.orderQueue = orderQueue;
        this.warehouse = warehouse;
        this.results = results;
        this.poisonPill = poisonPill;
    }

    @Override
    public void run() {
        System.out.printf("[WORKER-%d] Started working%n", workerId);

        while (true) {
            try {
                Order order = orderQueue.take();

                if (order == poisonPill) {
                    break;
                }

                System.out.printf("[WORKER-%d] Processing %s%n", workerId, order);
                OrderResult result = warehouse.processOrder(order);
                results.add(result);

                Thread.sleep(50);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("[WORKER-%d] Finished working%n", workerId);
    }
}