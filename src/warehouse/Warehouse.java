package warehouse;

import model.Order;
import model.OrderResult;
import model.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Warehouse {
    private final ConcurrentHashMap<Product, Integer> inventory;

    public Warehouse() {
        this.inventory = new ConcurrentHashMap<>();
    }

    public void addStock(Product product, int quantity) {
        inventory.put(product, inventory.getOrDefault(product, 0) + quantity);
    }

    public synchronized OrderResult processOrder(Order order){
        for(Map.Entry<Product, Integer> entry : order.getItems().entrySet()){
            Product product = entry.getKey();
            int requestedQuantity = entry.getValue();
            int availableQuantity = inventory.getOrDefault(product, 0);

            if(requestedQuantity > availableQuantity) {
                System.out.printf("[FAILED] %s - Not enough stock for %s (requested: %d, available: %d)%n",
                        order, product.name(), requestedQuantity, availableQuantity);
                return OrderResult.failure(order);
            }
        }
        return processSuccessfulOrder(order);
    }

    private OrderResult processSuccessfulOrder(Order order) {
        Map<Product, Integer> purchasedItems = new HashMap<>();
        double totalProfit = 0;

        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            inventory.compute(product, (_, currQuantity) -> currQuantity - quantity);
            purchasedItems.put(product, quantity);
            totalProfit += product.price() * quantity;
        }

        System.out.printf("[SUCCESS] %s - Total: $%.2f%n", order, totalProfit);
        return OrderResult.success(order, totalProfit);
    }

    public void displayInventory() {
        System.out.println("\n------Current Inventory------");
        inventory.forEach((product, quantity) ->
                System.out.printf("%s: %d units%n", product, quantity)
        );
        System.out.println();
    }
}
