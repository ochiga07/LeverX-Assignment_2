package model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Reservation {
    private static int reservationCounter = 0;
    private final int reservationId;
    private final String customerName;
    private final Map<Product, Integer> reservedItems;
    private final LocalDateTime createdAt;
    private boolean active;

    public Reservation(String customerName) {
        this.reservationId = ++reservationCounter;
        this.customerName = customerName;
        this.reservedItems = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public void addItem(Product product, int quantity) {
        reservedItems.put(product, reservedItems.getOrDefault(product, 0) + quantity);
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Map<Product, Integer> getReservedItems() {
        return Collections.unmodifiableMap(reservedItems);
    }

    public boolean isActive() {
        return active;
    }

    public void cancel() {
        this.active = false;
    }

    @Override
    public String toString() {
        return String.format("Reservation #%d (Customer: %s, Active: %s)",
                reservationId, customerName, active);
    }
}