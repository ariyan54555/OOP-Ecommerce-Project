package com.ecommerce.model.user;

/**
 * Seller user. Product/stock/order-status actions are carried out through
 * ProductManager / OrderManager (coordinated by ClientHandler), which
 * validate that the requesting seller actually owns the product/order -
 * that ownership check is what keeps this simple class free of manager
 * references while still being safe.
 * Demonstrates: Inheritance (extends User), Polymorphism (getRole()).
 */
public class Seller extends User {

    public Seller(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }
}
