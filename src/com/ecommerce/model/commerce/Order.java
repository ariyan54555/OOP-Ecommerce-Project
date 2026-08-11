package com.ecommerce.model.commerce;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public static final String PENDING = "Pending";
    public static final String CONFIRMED = "Confirmed";
    public static final String PACKED = "Packed";
    public static final String SHIPPED = "Shipped";
    public static final String DELIVERED = "Delivered";

    /** Valid lifecycle, in order. Used to validate seller status updates. */
    public static final String[] LIFECYCLE = {PENDING, CONFIRMED, PACKED, SHIPPED, DELIVERED};

    private int orderId;
    private String customerUsername;
    private List<OrderItem> items;
    private String status;

    public Order(int orderId, String customerUsername, List<OrderItem> items) {
        this.orderId = orderId;
        this.customerUsername = customerUsername;
        this.items = items;
        this.status = PENDING;
    }

    public Order(int orderId, String customerUsername, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerUsername = customerUsername;
        this.items = items;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public static boolean isValidStatus(String status) {
        for (String s : LIFECYCLE) {
            if (s.equalsIgnoreCase(status)) return true;
        }
        return false;
    }

    /** Encodes items as pid:qty|pid:qty for the response/file payload. */
    public String itemsToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(items.get(i).toString());
        }
        return sb.toString();
    }

    /** orderId,customerUsername,status,pid:qty|pid:qty - for file persistence. */
    @Override
    public String toString() {
        return orderId + "," + customerUsername + "," + status + "," + itemsToString();
    }

    public static Order fromFileLine(String line) {
        String[] parts = line.split(",", 4);
        int orderId = Integer.parseInt(parts[0]);
        String customerUsername = parts[1];
        String status = parts[2];
        List<OrderItem> items = new ArrayList<>();
        if (parts.length > 3 && !parts[3].isEmpty()) {
            for (String itemStr : parts[3].split("\\|")) {
                items.add(OrderItem.fromString(itemStr));
            }
        }
        return new Order(orderId, customerUsername, items, status);
    }
}
