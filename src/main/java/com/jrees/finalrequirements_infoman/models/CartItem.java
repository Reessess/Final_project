package com.jrees.finalrequirements_infoman.models;

import javafx.beans.property.*;

public class CartItem {
    private final IntegerProperty cartItemId;
    private final Product product;
    private final IntegerProperty quantity;

    // Constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.cartItemId = new SimpleIntegerProperty(0); // Default value until loaded from DB
    }

    // Getter and setter for cartItemId
    public int getCartItemId() {
        return cartItemId.get();
    }
    // Getter for product
    public Product getProduct() {
        return product;
    }
    // Getter and setter for quantity
    public int getQuantity() {
        return quantity.get();
    }
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
    // Total price for this cart item (price * quantity)
    public double getTotalPrice() {
        return product.getPrice() * getQuantity();
    }
    public IntegerProperty getQuantityProperty() {
        return quantity;
    }
    public DoubleProperty getTotalPriceProperty() {
        return new SimpleDoubleProperty(getTotalPrice());
    }
    // Override equals method to compare based on product (you may adjust this if needed)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return product.getProductId() == cartItem.product.getProductId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(product.getProductId());
    }

    // String representation for easier debugging or logging
    @Override
    public String toString() {
        return String.format("CartItem{id=%d, product=%s, quantity=%d, total=₱%.2f}",
                getCartItemId(), product.getName(), getQuantity(), getTotalPrice());
    }
}
