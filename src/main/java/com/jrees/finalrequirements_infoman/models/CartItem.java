package com.jrees.finalrequirements_infoman.models;

import javafx.beans.property.*;

public class CartItem {
    private final ObjectProperty<Product> product;
    private final IntegerProperty quantity;
    private final DoubleProperty totalPrice;

    public CartItem(Product product, int quantity) {
        this.product = new SimpleObjectProperty<>(product);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(product.getPrice() * quantity);

        // Listener to update total price if product price changes
        this.product.addListener((observable, oldValue, newValue) -> updateTotalPrice());

        // Listener to update total price if quantity changes
        this.quantity.addListener((observable, oldValue, newValue) -> updateTotalPrice());
    }

    public Product getProduct() {
        return product.get();
    }

    public ObjectProperty<Product> getProductProperty() {
        return product;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty getQuantityProperty() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public DoubleProperty getTotalPriceProperty() {
        return totalPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        // Recalculate total price based on product price and quantity
        this.totalPrice.set(product.get().getPrice() * quantity.get());
    }

    @Override
    public String toString() {
        return product.get().getName() + " - Quantity: " + quantity.get() + " - Total: " + totalPrice.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return product.get().getProductId() == cartItem.product.get().getProductId();
    }

    @Override
    public int hashCode() {
        return product.get().getProductId();
    }
}
