package warehouse;

import model.Order;
import model.OrderResult;
import model.Product;
import model.Reservation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Warehouse {
    private final ConcurrentHashMap<Product, Integer> inventory;
    private final ConcurrentHashMap<Product, Integer> reservedInventory;
    private final List<Reservation> reservations;

    public Warehouse() {
        this.inventory = new ConcurrentHashMap<>();
        this.reservedInventory = new ConcurrentHashMap<>();
        this.reservations = Collections.synchronizedList(new ArrayList<>());
    }

    public void addStock(Product product, int quantity) {
        inventory.put(product, inventory.getOrDefault(product, 0) + quantity);
        reservedInventory.putIfAbsent(product, 0);
    }

    public synchronized boolean reserveProducts(Reservation reservation) {
        // check if all items are available
        for (Map.Entry<Product, Integer> entry : reservation.getReservedItems().entrySet()) {
            Product product = entry.getKey();
            int requestedQuantity = entry.getValue();
            int availableQuantity = getAvailableQuantity(product);

            if (availableQuantity < requestedQuantity) {
                System.out.printf("[RESERVATION FAILED] %s - Not enough stock for %s (requested: %d, available: %d)%n",
                        reservation, product.getName(), requestedQuantity, availableQuantity);
                return false;
            }
        }

        // reserve the items
        reserveItems(reservation);
        return true;
    }

    public synchronized void reserveItems(Reservation reservation){
        for (Map.Entry<Product, Integer> entry : reservation.getReservedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            reservedInventory.compute(product, (_, currQuantity) -> currQuantity + quantity);
        }

        reservations.add(reservation);
        System.out.printf("[RESERVATION SUCCESS] %s created%n", reservation);
    }

    public synchronized boolean cancelReservation(int reservationId) {
        Reservation reservation = findReservation(reservationId);

        if (reservation == null || !reservation.isActive()) {
            System.out.printf("[CANCEL FAILED] Reservation #%d not found or already cancelled%n", reservationId);
            return false;
        }

        // cancel the reservation and back up products
        for (Map.Entry<Product, Integer> entry : reservation.getReservedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            reservedInventory.compute(product, (_, currQuantity) -> currQuantity - quantity);
        }

        reservation.cancel();
        System.out.printf("[RESERVATION CANCELLED] %s%n", reservation);
        return true;
    }

    public synchronized OrderResult processOrder(Order order) {
        // check if all items are available
        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product product = entry.getKey();
            int requestedQuantity = entry.getValue();
            int availableQuantity = getAvailableQuantity(product);

            if (availableQuantity < requestedQuantity) {
                System.out.printf("[ORDER FAILED] %s - Not enough stock for %s (requested: %d, available: %d)%n",
                        order, product.getName(), requestedQuantity, availableQuantity);
                return OrderResult.failure(order);
            }
        }

        // all items are available
        return processSuccessfulOrder(order);
    }

    private OrderResult processSuccessfulOrder(Order order){
        Map<Product, Integer> purchasedItems = new HashMap<>();
        double totalProfit = 0;

        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            inventory.compute(product, (_, currQuantity) -> currQuantity - quantity);
            purchasedItems.put(product, quantity);
            totalProfit += product.getPrice() * quantity;
        }

        System.out.printf("[ORDER SUCCESS] %s - Total: $%.2f%n", order, totalProfit);
        return OrderResult.success(order, totalProfit, purchasedItems);
    }

    private int getAvailableQuantity(Product product) {
        int totalStock = inventory.getOrDefault(product, 0);
        int reserved = reservedInventory.getOrDefault(product, 0);
        return totalStock - reserved;
    }

    private Reservation findReservation(int reservationId) {
        return reservations.stream()
                .filter(r -> r.getReservationId() == reservationId)
                .findFirst()
                .orElse(null);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void displayInventory() {
        System.out.println("\n-----Current Inventory-----");
        inventory.forEach((product, quantity) -> {
            int reserved = reservedInventory.getOrDefault(product, 0);
            int available = quantity - reserved;
            System.out.printf("%s: %d total (%d available, %d reserved)%n",
                    product, quantity, available, reserved);
        });
        System.out.println();
    }
}