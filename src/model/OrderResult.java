package model;

import java.util.Collections;
import java.util.Map;

public class OrderResult {
    private final Order order;
    private final boolean success;
    private final double profit;
    private final Map<Product, Integer> purchasedItems;

    private OrderResult(Order order, boolean success, double profit, Map<Product, Integer> purchasedItems) {
        this.order = order;
        this.success = success;
        this.profit = profit;
        this.purchasedItems = purchasedItems;
    }

    public static OrderResult success(Order order, double profit, Map<Product, Integer> purchasedItems) {
        return new OrderResult(order, true, profit, purchasedItems);
    }

    public static OrderResult failure(Order order) {
        return new OrderResult(order, false, 0, Collections.emptyMap());
    }

    public Order getOrder() {
        return order;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getProfit() {
        return profit;
    }

    public Map<Product, Integer> getPurchasedItems() {
        return purchasedItems;
    }
}