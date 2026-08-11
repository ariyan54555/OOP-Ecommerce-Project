package com.ecommerce.model.user;

import com.ecommerce.model.commerce.Order;
import com.ecommerce.model.commerce.OrderItem;
import com.ecommerce.model.commerce.Product;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    private List<OrderItem> cart;

    public Customer(String username, String password) {
        super(username, password);
        this.cart = new ArrayList<>();
    }

    public List<OrderItem> getCart() {
        return cart;
    }

    public void addToCart(Product product, int qty) {
        addToCart(product.getId(), qty);
    }

    public void addToCart(int productId, int qty) {
        for (OrderItem item : cart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + qty);
                return;
            }
        }
        cart.add(new OrderItem(productId, qty));
    }

    public void clearCart() {
        cart.clear();
    }

    /**
     * Packages the current cart into a new Order and empties the cart.
     * Stock validation/deduction is handled by ProductManager (see
     * ClientHandler.handleCheckout) since that is where the synchronized,
     * file-backed inventory lives.
     */
    public Order checkout(int orderId) {
        if (cart.isEmpty()) {
            return null;
        }
        List<OrderItem> items = new ArrayList<>(cart);
        Order order = new Order(orderId, username, items);
        clearCart();
        return order;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }
}
