import actors.Customer;
import actors.WarehouseWorker;
import analytics.Analytics;
import model.Order;
import model.OrderResult;
import model.Product;
import model.Reservation;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class OnlineShop {

    private static final int NUM_CUSTOMERS = 5;
    private static final int NUM_WORKERS = 3;
    private static final int ORDERS_PER_CUSTOMER = 3;
    private static final int RESERVATIONS_PER_CUSTOMER = 2;

    private static final int ORDER_QUEUE_CAPACITY = 20;
    private static final int CUSTOMER_TIMEOUT_SEC = 10;
    private static final int WORKER_TIMEOUT_SEC = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("-----ONLINE SHOP SIMULATION WITH RESERVATIONS----");

        List<Product> catalog = createCatalog();
        Warehouse warehouse = initializeWarehouse(catalog);
        warehouse.displayInventory();

        BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>(ORDER_QUEUE_CAPACITY);
        List<OrderResult> results = Collections.synchronizedList(new ArrayList<>());
        Order poisonPill = new Order("POISON_PILL");

        // reservation phase
        System.out.println("PHASE 1: CUSTOMERS CREATE RESERVATIONS");
        createReservations(catalog, warehouse);
        warehouse.displayInventory();

        System.out.println("PHASE 2: CANCEL SOME RESERVATIONS");
        cancelSomeReservations(warehouse);
        warehouse.displayInventory();

        // order processing phase
        System.out.println("PHASE 3: STARTING WORKERS");
        ExecutorService workerService = runWorkers(orderQueue, warehouse, results, poisonPill);

        System.out.println("PHASE 4: CUSTOMERS PLACING ORDERS");
        runCustomers(orderQueue, catalog);

        // all customers finished, add NUM_WORKERS poison pills
        for (int i = 0; i < NUM_WORKERS; i++) {
            orderQueue.put(poisonPill);
        }

        // Shutdown workers
        workerService.shutdown();
        if (!workerService.awaitTermination(WORKER_TIMEOUT_SEC, TimeUnit.SECONDS)) {
            System.out.println("Worker threads did not finish in time!");
        }

        warehouse.displayInventory();

        Analytics.runAnalytics(results, warehouse);
    }


    private static List<Product> createCatalog() {
        return Arrays.asList(
                new Product("Laptop", 1000),
                new Product("Mouse", 30),
                new Product("Keyboard", 80),
                new Product("Monitor", 300),
                new Product("Headphones", 150)
        );
    }

    private static Warehouse initializeWarehouse(List<Product> catalog) {
        Warehouse warehouse = new Warehouse();
        warehouse.addStock(catalog.get(0), 10); // Laptop
        warehouse.addStock(catalog.get(1), 20); // Mouse
        warehouse.addStock(catalog.get(2), 15); // Keyboard
        warehouse.addStock(catalog.get(3), 8);  // Monitor
        warehouse.addStock(catalog.get(4), 12); // Headphones
        return warehouse;
    }

    private static void createReservations(List<Product> catalog, Warehouse warehouse) {
        Random random = new Random();

        for (int c = 1; c <= NUM_CUSTOMERS; c++) {
            for (int r = 0; r < RESERVATIONS_PER_CUSTOMER; r++) {

                Reservation reservation = new Reservation("Customer-" + c);

                int itemCount = random.nextInt(2) + 1;
                for (int k = 0; k < itemCount; k++) {
                    Product product = catalog.get(random.nextInt(catalog.size()));
                    int quantity = random.nextInt(5) + 1;
                    reservation.addItem(product, quantity);
                }

                warehouse.reserveProducts(reservation);
            }
        }
    }

    private static void cancelSomeReservations(Warehouse warehouse) {
        Random random = new Random();

        List<Reservation> reservations = warehouse.getReservations();
        int toCancel = reservations.size() / 2;

        for (int i = 0; i < toCancel; i++) {
            int reservationId = random.nextInt(reservations.size()) + 1;
            warehouse.cancelReservation(reservationId);
        }
    }


    private static void runCustomers(BlockingQueue<Order> queue, List<Product> catalog)
            throws InterruptedException {

        ExecutorService customerService = Executors.newFixedThreadPool(NUM_CUSTOMERS);

        for (int i = 1; i <= NUM_CUSTOMERS; i++) {
            customerService.submit(new Customer(
                    "Customer-" + i,
                    queue,
                    catalog,
                    ORDERS_PER_CUSTOMER
            ));
        }

        customerService.shutdown();
        if (!customerService.awaitTermination(CUSTOMER_TIMEOUT_SEC, TimeUnit.SECONDS)) {
            System.out.println("Customer threads did not finish in time!");
        }
    }

    private static ExecutorService runWorkers(BlockingQueue<Order> orderQueue, Warehouse warehouse,
                                              List<OrderResult> results, Order poisonPill) {

        ExecutorService workerService = Executors.newFixedThreadPool(NUM_WORKERS);
        for (int i = 1; i <= NUM_WORKERS; i++) {
            workerService.submit(new WarehouseWorker(
                    i,
                    orderQueue,
                    warehouse,
                    results,
                    poisonPill
            ));
        }
        return workerService;
    }
}