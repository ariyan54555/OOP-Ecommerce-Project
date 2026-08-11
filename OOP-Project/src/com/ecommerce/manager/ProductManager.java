package com.ecommerce.manager;

import com.ecommerce.model.commerce.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the centralized, in-memory product inventory and its file backing
 * (data/products.txt). purchase(id, qty) is the project's core technical
 * highlight: it is synchronized on the manager itself so that, for every
 * purchase across the whole catalog, the stock check, the stock update,
 * and the file save happen together as one atomic unit. That is what
 * prevents two customers from both "successfully" buying the last units
 * of a product at the same time (overselling).
 */
public class ProductManager {

    private static final String FILE_PATH = "data/products.txt";
    private List<Product> products;
    private int nextId;

    public ProductManager() {
        products = new ArrayList<>();
        loadProducts();
    }

    public synchronized void loadProducts() {
        products.clear();
        int maxId = 0;
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            nextId = 1;
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 5);
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);
                int stock = Integer.parseInt(parts[3]);
                String seller = parts[4];
                products.add(new Product(id, name, price, stock, seller));
                maxId = Math.max(maxId, id);
            }
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        nextId = maxId + 1;
    }

    public synchronized void saveProducts() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Product p : products) {
                pw.println(p.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
        }
    }

    public synchronized Product findById(int id) {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public synchronized List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public synchronized List<Product> searchByName(String keyword) {
        List<Product> result = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(lower)) {
                result.add(p);
            }
        }
        return result;
    }

    public synchronized String addProduct(String name, double price, int stock, String sellerUsername) {
        if (name == null || name.trim().isEmpty() || price < 0 || stock < 0) {
            return "FAIL:Invalid product details";
        }
        Product p = new Product(nextId++, name, price, stock, sellerUsername);
        products.add(p);
        saveProducts();
        return "SUCCESS:" + p.getId();
    }

    /** Corner case: product not found returns a clear response, not a crash. */
    public synchronized String updateProduct(int id, String sellerUsername, String name, double price) {
        Product p = findById(id);
        if (p == null) return "FAIL:Product not found";
        if (!p.getSellerUsername().equals(sellerUsername)) return "FAIL:Not your product";
        if (name != null && !name.trim().isEmpty()) p.setName(name);
        if (price >= 0) p.setPrice(price);
        saveProducts();
        return "SUCCESS";
    }

    public synchronized String updateStock(int id, String sellerUsername, int newStock) {
        Product p = findById(id);
        if (p == null) return "FAIL:Product not found";
        if (!p.getSellerUsername().equals(sellerUsername)) return "FAIL:Not your product";
        if (newStock < 0) return "FAIL:Stock cannot be negative";
        p.setStock(newStock);
        saveProducts();
        return "SUCCESS";
    }

    /**
     * The synchronized purchase flow. Only one thread (one customer's
     * request) can execute this method at a time across the whole catalog,
     * so a stock check can never be invalidated by another thread's update
     * before the file is saved.
     */
    public synchronized String purchase(int id, int qty) {
        if (qty <= 0) {
            return "FAIL:Invalid quantity";
        }
        Product p = findById(id);
        if (p == null) {
            return "FAIL:Product not found";
        }
        if (p.getStock() < qty) {
            return "FAIL:Only " + p.getStock() + " items available.";
        }
        p.setStock(p.getStock() - qty);
        saveProducts();
        return "SUCCESS";
    }

    /** Used to roll back stock if a later item in a multi-item checkout fails. */
    public synchronized void restock(int id, int qty) {
        Product p = findById(id);
        if (p != null) {
            p.setStock(p.getStock() + qty);
            saveProducts();
        }
    }
}
