package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Order {
    private static int orderCounter = 0;
    private final int orderId;
    private final Map<Product, Integer> items;
    private final String customerName;


    public Order(String customerName) {
        this.orderId = ++orderCounter;
        this.customerName = customerName;
        this.items = new HashMap<>();
    }

    public void addItem(Product product, int quantity){
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }


    public Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }


    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}
