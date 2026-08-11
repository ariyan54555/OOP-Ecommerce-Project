package com.ecommerce.model.commerce;

public class Product {

    private int id;
    private String name;
    private double price;
    private int stock;
    private String sellerUsername;

    public Product(int id, String name, double price, int stock, String sellerUsername) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.sellerUsername = sellerUsername;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    /**
     * Attempts to deduct qty from stock. The authoritative, file-synchronized
     * version of this check lives in ProductManager.purchase(id, qty) - that
     * is the method actually called from ClientHandler during checkout, so
     * that stock-check + stock-update + file-save happen as one atomic,
     * synchronized unit (see corner case: "Concurrent file writes").
     * This instance method is kept for completeness / unit testing in
     * isolation from the manager and file system.
     */
    public synchronized String purchase(int qty) {
        if (qty <= 0) {
            return "FAIL:Invalid quantity";
        }
        if (stock < qty) {
            return "FAIL:Only " + stock + " items available.";
        }
        stock -= qty;
        return "SUCCESS";
    }

    /** id,name,price,stock,sellerUsername - for file persistence. */
    @Override
    public String toString() {
        return id + "," + name + "," + price + "," + stock + "," + sellerUsername;
    }
}
