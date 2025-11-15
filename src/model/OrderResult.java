package model;

import java.util.Map;

public class OrderResult {
    private final Order order;
    private final boolean success;
    private final double profit;
    private final Map<Product, Integer> purchasedItems;

    private OrderResult(Order order, boolean success, double profit) {
        this.order = order;
        this.success = success;
        this.profit = profit;
        this.purchasedItems = order.getItems();
    }

    public static OrderResult success(Order order, double profit) {
        return new OrderResult(order, true, profit);
    }

    public static OrderResult failure(Order order) {
        return new OrderResult(order, false, 0);
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

    @Override
    public String toString() {
        return "OrderResult{" +
                "order" + order +
                ", success=" + success +
                ", profit=" + profit +
                ", purchasedItems=" + purchasedItems +
                '}';
    }
}