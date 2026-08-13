package com.ecommerce.manager;

import com.ecommerce.model.commerce.Order;
import com.ecommerce.model.commerce.OrderItem;
import com.ecommerce.model.commerce.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private static final String FILE_PATH = "data/orders.txt";
    private List<Order> orders;
    private int nextOrderId;

    public OrderManager() {
        orders = new ArrayList<>();
        loadOrders();
    }

    public synchronized void loadOrders() {
        orders.clear();
        int maxId = 0;
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            nextOrderId = 1;
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Order o = Order.fromFileLine(line);
                orders.add(o);
                maxId = Math.max(maxId, o.getOrderId());
            }
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        nextOrderId = maxId + 1;
    }

    public synchronized void saveOrders() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Order o : orders) {
                pw.println(o.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving orders: " + e.getMessage());
        }
    }

    public synchronized int reserveNextOrderId() {

        return nextOrderId++;
    }

    public synchronized String placeOrder(Order order) {
        orders.add(order);
        saveOrders();
        return "SUCCESS:" + order.getOrderId();
    }

    public synchronized Order findById(int orderId) {
        for (Order o : orders) {
            if (o.getOrderId() == orderId) return o;
        }
        return null;
    }

    public synchronized List<Order> getOrdersByCustomer(String username) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getCustomerUsername().equals(username)) result.add(o);
        }
        return result;
    }

   
    public synchronized List<Order> getOrdersBySeller(String sellerUsername, ProductManager productManager) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            for (OrderItem item : o.getItems()) {
                Product p = productManager.findById(item.getProductId());
                if (p != null && p.getSellerUsername().equals(sellerUsername)) {
                    result.add(o);
                    break;
                }
            }
        }
        return result;
    }

 
    public synchronized String updateStatus(int orderId, String sellerUsername, String newStatus,
                                             ProductManager productManager) {
        Order order = findById(orderId);
        if (order == null) return "FAIL:Order not found";
        if (!Order.isValidStatus(newStatus)) return "FAIL:Invalid status";

        boolean ownsItem = false;
        for (OrderItem item : order.getItems()) {
            Product p = productManager.findById(item.getProductId());
            if (p != null && p.getSellerUsername().equals(sellerUsername)) {
                ownsItem = true;
                break;
            }
        }
        if (!ownsItem) return "FAIL:You do not have a product in this order";

        order.updateStatus(newStatus);
        saveOrders();
        return "SUCCESS";
    }
}
