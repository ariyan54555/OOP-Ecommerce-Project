package com.ecommerce.model.commerce;

/**
 * Represents a single line item inside a Cart or an Order:
 * a reference to a product id plus the quantity requested.
 */
public class OrderItem {

    private int productId;
    private int quantity;

    public OrderItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** productId:quantity - used inside Order persistence and wire responses. */
    @Override
    public String toString() {
        return productId + ":" + quantity;
    }

    public static OrderItem fromString(String s) {
        String[] parts = s.split(":");
        return new OrderItem(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
