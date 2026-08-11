package com.ecommerce.client;

import java.io.IOException;
import java.util.Scanner;

/**
 * Console entry point for a Customer or Seller. Connects to the Server,
 * logs in / registers, then loops a role-specific menu, translating each
 * choice into a protocol request and printing a friendly rendering of the
 * response.
 */
public class ClientApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static Client client;
    private static String role;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = 5000;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        client = new Client(host, port);
        try {
            client.connect();
            System.out.println("Connected to server at " + host + ":" + port);
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
            System.out.println("Make sure the Server is running first.");
            return;
        }

        authMenu();

        if (role != null) {
            if (role.equals("CUSTOMER")) {
                customerMenu();
            } else {
                sellerMenu();
            }
        }

        client.close();
        System.out.println("Disconnected. Goodbye!");
    }

    // ---------------------------------------------------------------
    // Auth
    // ---------------------------------------------------------------

    private static void authMenu() {
        while (role == null) {
            System.out.println("\n=== Welcome ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    doRegister();
                    break;
                case "2":
                    doLogin();
                    break;
                case "3":
                    sendAndIgnore("EXIT:");
                    client.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void doRegister() {
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role (1=Customer, 2=Seller): ");
        String roleChoice = scanner.nextLine().trim();
        String roleStr = roleChoice.equals("2") ? "SELLER" : "CUSTOMER";

        String response = send("REGISTER:" + username + "," + password + "," + roleStr);
        if (response == null) return;
        if (response.startsWith("SUCCESS")) {
            System.out.println("Registration successful! You can now log in.");
        } else {
            System.out.println("Registration failed: " + afterColon(response));
        }
    }

    private static void doLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String response = send("LOGIN:" + username + "," + password);
        if (response == null) return;
        if (response.startsWith("SUCCESS")) {
            role = afterColon(response);
            System.out.println("Login successful! Welcome, " + username + " (" + role + ")");
        } else {
            System.out.println("Login failed: " + afterColon(response));
        }
    }

    // ---------------------------------------------------------------
    // Customer menu
    // ---------------------------------------------------------------

    private static void customerMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. View all products");
            System.out.println("2. Search products");
            System.out.println("3. Add product to cart");
            System.out.println("4. View cart");
            System.out.println("5. Checkout");
            System.out.println("6. View order history");
            System.out.println("7. Check order status");
            System.out.println("8. Logout / Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    printProducts(send("LIST_PRODUCTS:"));
                    break;
                case "2":
                    System.out.print("Search keyword: ");
                    printProducts(send("SEARCH:" + scanner.nextLine().trim()));
                    break;
                case "3": {
                    System.out.print("Product ID: ");
                    String pid = scanner.nextLine().trim();
                    System.out.print("Quantity: ");
                    String qty = scanner.nextLine().trim();
                    printResult(send("ADD_TO_CART:" + pid + "," + qty));
                    break;
                }
                case "4":
                    printCart(send("VIEW_CART:"));
                    break;
                case "5":
                    printCheckoutResult(send("CHECKOUT:"));
                    break;
                case "6":
                    printOrders(send("ORDER_HISTORY:"));
                    break;
                case "7":
                    System.out.print("Order ID: ");
                    printResult(send("CHECK_STATUS:" + scanner.nextLine().trim()));
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Seller menu
    // ---------------------------------------------------------------

    private static void sellerMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Seller Menu ===");
            System.out.println("1. Add new product");
            System.out.println("2. Update product (name/price)");
            System.out.println("3. Update stock quantity");
            System.out.println("4. View my products");
            System.out.println("5. View orders for my products");
            System.out.println("6. Update order status");
            System.out.println("7. Logout / Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    System.out.print("Product name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Price: ");
                    String price = scanner.nextLine().trim();
                    System.out.print("Initial stock: ");
                    String stock = scanner.nextLine().trim();
                    printResult(send("ADD_PRODUCT:" + name + "," + price + "," + stock));
                    break;
                }
                case "2": {
                    System.out.print("Product ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.print("New name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("New price: ");
                    String price = scanner.nextLine().trim();
                    printResult(send("UPDATE_PRODUCT:" + id + "," + name + "," + price));
                    break;
                }
                case "3": {
                    System.out.print("Product ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.print("New stock quantity: ");
                    String qty = scanner.nextLine().trim();
                    printResult(send("UPDATE_STOCK:" + id + "," + qty));
                    break;
                }
                case "4":
                    printProducts(send("LIST_PRODUCTS:"));
                    break;
                case "5":
                    printOrders(send("SELLER_ORDERS:"));
                    break;
                case "6": {
                    System.out.print("Order ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.println("Status options: Pending, Confirmed, Packed, Shipped, Delivered");
                    System.out.print("New status: ");
                    String status = scanner.nextLine().trim();
                    printResult(send("UPDATE_ORDER_STATUS:" + id + "," + status));
                    break;
                }
                case "7":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Networking + response rendering helpers
    // ---------------------------------------------------------------

    private static String send(String request) {
        try {
            return client.sendRequest(request);
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
            return null;
        }
    }

    private static void sendAndIgnore(String request) {
        try {
            client.sendRequest(request);
        } catch (IOException ignored) {
        }
    }

    private static String afterColon(String response) {
        int idx = response.indexOf(':');
        return idx == -1 ? "" : response.substring(idx + 1);
    }

    private static void printResult(String response) {
        if (response == null) return;
        if (response.startsWith("SUCCESS")) {
            String data = afterColon(response);
            System.out.println(data.isEmpty() ? "Done." : "OK: " + data);
        } else {
            System.out.println("Error: " + afterColon(response));
        }
    }

    private static void printCheckoutResult(String response) {
        if (response == null) return;
        if (response.startsWith("SUCCESS")) {
            System.out.println("Order placed successfully! Order ID: " + afterColon(response));
        } else {
            System.out.println("Checkout failed: " + afterColon(response));
        }
    }

    private static void printProducts(String response) {
        if (response == null) return;
        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + afterColon(response));
            return;
        }
        String data = afterColon(response);
        if (data.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.printf("%-5s %-20s %-10s %-8s %-15s%n", "ID", "Name", "Price", "Stock", "Seller");
        for (String record : data.split(";")) {
            String[] f = record.split(",", -1);
            System.out.printf("%-5s %-20s %-10s %-8s %-15s%n", f[0], f[1], f[2], f[3], f[4]);
        }
    }

    private static void printCart(String response) {
        if (response == null) return;
        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + afterColon(response));
            return;
        }
        String data = afterColon(response);
        if (data.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.printf("%-5s %-20s %-10s %-6s%n", "ID", "Name", "Price", "Qty");
        for (String record : data.split(";")) {
            String[] f = record.split(",", -1);
            System.out.printf("%-5s %-20s %-10s %-6s%n", f[0], f[1], f[2], f[3]);
        }
    }

    private static void printOrders(String response) {
        if (response == null) return;
        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + afterColon(response));
            return;
        }
        String data = afterColon(response);
        if (data.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.printf("%-10s %-12s %-30s%n", "Order ID", "Status", "Items (productId:qty)");
        for (String record : data.split(";")) {
            String[] f = record.split(",", 3);
            String items = f.length > 2 ? f[2] : "";
            System.out.printf("%-10s %-12s %-30s%n", f[0], f[1], items);
        }
    }
}
