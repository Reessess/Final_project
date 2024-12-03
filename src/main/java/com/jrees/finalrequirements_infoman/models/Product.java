package com.jrees.finalrequirements_infoman.models;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty productId;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty quantity;

    public Product(int productId, String name, double price, int quantity) {
        this.productId = new SimpleIntegerProperty(productId);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    // Property Getters
    public IntegerProperty getProductIdProperty() {
        return productId;
    }

    public StringProperty getNameProperty() {
        return name;
    }

    public DoubleProperty getPriceProperty() {
        return price;
    }

    public IntegerProperty getQuantityProperty() {
        return quantity;
    }

    // Regular Getters
    public int getProductId() {
        return productId.get();
    }

    public String getName() {
        return name.get();
    }

    public double getPrice() {
        return price.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    // Regular Setters
    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    // Method to decrease stock when added to cart
    public void decreaseQuantity(int quantityToDecrease) {
        int currentQuantity = this.quantity.get();
        if (quantityToDecrease <= currentQuantity) {
            this.quantity.set(currentQuantity - quantityToDecrease);
        } else {
            throw new IllegalArgumentException("Not enough stock available.");
        }
    }

    // Method to increase stock when removing from cart
    public void increaseQuantity(int quantityToIncrease) {
        int currentQuantity = this.quantity.get();
        this.quantity.set(currentQuantity + quantityToIncrease);
    }

    // Method to check if a product's name matches a search query (case-insensitive)
    public boolean matchesSearchQuery(String query) {
        return name.get().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("Product ID: %d, Name: %s, Price: %.2f, Quantity: %d",
                getProductId(), getName(), getPrice(), getQuantity());
    }
}
