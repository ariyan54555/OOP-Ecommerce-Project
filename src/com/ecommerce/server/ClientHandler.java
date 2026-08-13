package com.ecommerce.server;

import com.ecommerce.manager.OrderManager;
import com.ecommerce.manager.ProductManager;
import com.ecommerce.manager.UserManager;
import com.ecommerce.model.commerce.Order;
import com.ecommerce.model.commerce.OrderItem;
import com.ecommerce.model.commerce.Product;
import com.ecommerce.model.user.Customer;
import com.ecommerce.model.user.Seller;
import com.ecommerce.model.user.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final OrderManager orderManager;

    private User currentUser;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, UserManager userManager,
                          ProductManager productManager, OrderManager orderManager) {
        this.socket = socket;
        this.userManager = userManager;
        this.productManager = productManager;
        this.orderManager = orderManager;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String request;
            while ((request = in.readLine()) != null) {
                String response = handleRequest(request);
                out.println(response);
                if (request.toUpperCase().startsWith("EXIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            String who = (currentUser != null) ? currentUser.getUsername() : "unknown client";
            System.out.println("[-] Disconnected: " + who);
        }
    }

   
    private String handleRequest(String request) {
        String command;
        String payload;
        int idx = request.indexOf(':');
        if (idx == -1) {
            command = request.trim();
            payload = "";
        } else {
            command = request.substring(0, idx).trim();
            payload = request.substring(idx + 1);
        }

        try {
            switch (command.toUpperCase()) {
                case "REGISTER": return handleRegister(payload);
                case "LOGIN": return handleLogin(payload);
                case "LIST_PRODUCTS": return handleListProducts();
                case "SEARCH": return handleSearch(payload);
                case "ADD_TO_CART": return handleAddToCart(payload);
                case "VIEW_CART": return handleViewCart();
                case "CHECKOUT": return handleCheckout();
                case "ORDER_HISTORY": return handleOrderHistory();
                case "CHECK_STATUS": return handleCheckStatus(payload);
                case "ADD_PRODUCT": return handleAddProduct(payload);
                case "UPDATE_PRODUCT": return handleUpdateProduct(payload);
                case "UPDATE_STOCK": return handleUpdateStock(payload);
                case "SELLER_ORDERS": return handleSellerOrders();
                case "UPDATE_ORDER_STATUS": return handleUpdateOrderStatus(payload);
                case "EXIT": return "BYE:Goodbye";
                default: return "FAIL:Unknown command";
            }
        } catch (Exception e) {
            return "FAIL:Malformed request";
        }
    }

 
    private String handleRegister(String payload) {
        String[] parts = payload.split(",", -1);
        if (parts.length != 3) return "FAIL:Expected username,password,role";
        return userManager.register(parts[0], parts[1], parts[2]);
    }

    private String handleLogin(String payload) {
        String[] parts = payload.split(",", -1);
        if (parts.length != 2) return "FAIL:Expected username,password";
        User u = userManager.login(parts[0], parts[1]);
        if (u == null) return "FAIL:Invalid username or password";
        currentUser = u;
        return "SUCCESS:" + u.getRole();
    }

    private String handleListProducts() {
        return encodeProducts(productManager.getAllProducts());
    }

    private String handleSearch(String payload) {
        if (payload.trim().isEmpty()) return "FAIL:Empty search keyword";
        return encodeProducts(productManager.searchByName(payload.trim()));
    }

    private String encodeProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder("SUCCESS:");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) sb.append(";");
            Product p = products.get(i);
            sb.append(p.getId()).append(",").append(p.getName()).append(",")
                    .append(p.getPrice()).append(",").append(p.getStock()).append(",")
                    .append(p.getSellerUsername());
        }
        return sb.toString();
    }


    private String handleAddToCart(String payload) {
        if (!(currentUser instanceof Customer)) return "FAIL:Login as a customer first";
        String[] parts = payload.split(",", -1);
        if (parts.length != 2) return "FAIL:Expected productId,qty";
        int productId, qty;
        try {
            productId = Integer.parseInt(parts[0].trim());
            qty = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return "FAIL:productId and qty must be numbers";
        }
        if (qty <= 0) return "FAIL:Invalid quantity";

        Product p = productManager.findById(productId);
        if (p == null) return "FAIL:Product not found";

        ((Customer) currentUser).addToCart(productId, qty);
        String warning = (qty > p.getStock()) ? " (warning: only " + p.getStock() + " in stock right now)" : "";
        return "SUCCESS:Added to cart" + warning;
    }

    private String handleViewCart() {
        if (!(currentUser instanceof Customer)) return "FAIL:Login as a customer first";
        List<OrderItem> cart = ((Customer) currentUser).getCart();
        StringBuilder sb = new StringBuilder("SUCCESS:");
        for (int i = 0; i < cart.size(); i++) {
            if (i > 0) sb.append(";");
            OrderItem item = cart.get(i);
            Product p = productManager.findById(item.getProductId());
            String name = (p != null) ? p.getName() : "(removed product)";
            double price = (p != null) ? p.getPrice() : 0.0;
            sb.append(item.getProductId()).append(",").append(name).append(",")
                    .append(price).append(",").append(item.getQuantity());
        }
        return sb.toString();
    }

   
    private String handleCheckout() {
        if (!(currentUser instanceof Customer)) return "FAIL:Login as a customer first";
        Customer customer = (Customer) currentUser;
        List<OrderItem> cart = customer.getCart();
        if (cart.isEmpty()) return "FAIL:Cart is empty";

        List<OrderItem> purchased = new ArrayList<>();
        for (OrderItem item : cart) {
            String result = productManager.purchase(item.getProductId(), item.getQuantity());
            if (result.equals("SUCCESS")) {
                purchased.add(item);
            } else {
                for (OrderItem done : purchased) {
                    productManager.restock(done.getProductId(), done.getQuantity());
                }
                String reason = result.contains(":") ? result.substring(result.indexOf(':') + 1) : result;
                return "FAIL:Product " + item.getProductId() + " - " + reason;
            }
        }

        int orderId = orderManager.reserveNextOrderId();
        Order order = customer.checkout(orderId);
        orderManager.placeOrder(order);
        return "SUCCESS:" + orderId;
    }

    private String handleOrderHistory() {
        if (!(currentUser instanceof Customer)) return "FAIL:Login as a customer first";
        return encodeOrders(orderManager.getOrdersByCustomer(currentUser.getUsername()));
    }

    private String handleCheckStatus(String payload) {
        if (currentUser == null) return "FAIL:Login first";
        int orderId;
        try {
            orderId = Integer.parseInt(payload.trim());
        } catch (NumberFormatException e) {
            return "FAIL:orderId must be a number";
        }
        Order o = orderManager.findById(orderId);
        if (o == null) return "FAIL:Order not found";
        return "SUCCESS:" + o.getStatus();
    }


    private String handleAddProduct(String payload) {
        if (!(currentUser instanceof Seller)) return "FAIL:Login as a seller first";
        String[] parts = payload.split(",", -1);
        if (parts.length != 3) return "FAIL:Expected name,price,stock";
        try {
            String name = parts[0];
            double price = Double.parseDouble(parts[1].trim());
            int stock = Integer.parseInt(parts[2].trim());
            return productManager.addProduct(name, price, stock, currentUser.getUsername());
        } catch (NumberFormatException e) {
            return "FAIL:price/stock must be numbers";
        }
    }

    private String handleUpdateProduct(String payload) {
        if (!(currentUser instanceof Seller)) return "FAIL:Login as a seller first";
        String[] parts = payload.split(",", -1);
        if (parts.length != 3) return "FAIL:Expected id,name,price";
        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1];
            double price = Double.parseDouble(parts[2].trim());
            return productManager.updateProduct(id, currentUser.getUsername(), name, price);
        } catch (NumberFormatException e) {
            return "FAIL:id/price must be numbers";
        }
    }

    private String handleUpdateStock(String payload) {
        if (!(currentUser instanceof Seller)) return "FAIL:Login as a seller first";
        String[] parts = payload.split(",", -1);
        if (parts.length != 2) return "FAIL:Expected id,qty";
        try {
            int id = Integer.parseInt(parts[0].trim());
            int qty = Integer.parseInt(parts[1].trim());
            return productManager.updateStock(id, currentUser.getUsername(), qty);
        } catch (NumberFormatException e) {
            return "FAIL:id/qty must be numbers";
        }
    }

    private String handleSellerOrders() {
        if (!(currentUser instanceof Seller)) return "FAIL:Login as a seller first";
        return encodeOrders(orderManager.getOrdersBySeller(currentUser.getUsername(), productManager));
    }

    private String handleUpdateOrderStatus(String payload) {
        if (!(currentUser instanceof Seller)) return "FAIL:Login as a seller first";
        String[] parts = payload.split(",", -1);
        if (parts.length != 2) return "FAIL:Expected orderId,status";
        try {
            int orderId = Integer.parseInt(parts[0].trim());
            String status = parts[1].trim();
            return orderManager.updateStatus(orderId, currentUser.getUsername(), status, productManager);
        } catch (NumberFormatException e) {
            return "FAIL:orderId must be a number";
        }
    }


    private String encodeOrders(List<Order> orders) {
        StringBuilder sb = new StringBuilder("SUCCESS:");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(";");
            Order o = orders.get(i);
            sb.append(o.getOrderId()).append(",").append(o.getStatus()).append(",")
                    .append(o.itemsToString());
        }
        return sb.toString();
    }
}
