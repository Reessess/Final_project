package com.jrees.finalrequirements_infoman.models;

import com.jrees.finalrequirements_infoman.utility.Database;
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

    // Method to update stock quantity (handles both increase and decrease)
    public boolean updateQuantity(int quantityToUpdate) {
        int currentQuantity = this.quantity.get();
        int newQuantity = currentQuantity + quantityToUpdate;

        if (newQuantity < 0) {
            System.out.println("Insufficient stock.");
            return false;  // Not enough stock
        }

        this.quantity.set(newQuantity);

        // Update the stock in the database
        Database db = new Database();
        db.updateProductStock(this.productId.get(), newQuantity);  // Update stock in DB
        db.closeConnection();
        return true;  // Success
    }

    // Method to decrease stock when added to cart
    public boolean decreaseQuantity(int quantityToDecrease) {
        return updateQuantity(-quantityToDecrease);
    }

    // Method to increase stock when removing from cart
    public void increaseQuantity(int quantityToIncrease) {
        updateQuantity(quantityToIncrease);
    }

    // Method to get available stock
    public int getStockAvailable() {
        return quantity.get();
    }

    // Method to check if a product's name matches a search query (case-insensitive)
    public boolean matchesSearchQuery(String query) {
        return name.get().toLowerCase().contains(query.toLowerCase());
    }

    // Method to get price as a formatted string
    public String getFormattedPrice() {
        return String.format("%.2f", price.get());
    }

    // Method to get the product's details formatted for the receipt
    public String getReceiptLine(int quantity) {
        return String.format("%s - Qty: %d - ₱%.2f", name.get(), quantity, price.get() * quantity);
    }

    // String representation of the product
    @Override
    public String toString() {
        return String.format("Product ID: %d, Name: %s, Price: ₱%.2f, Quantity Available: %d",
                getProductId(), getName(), getPrice(), getQuantity());
    }

    // Equality check based on product ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId.get() == product.productId.get();
    }

    // HashCode based on product ID for consistent equality checks
    @Override
    public int hashCode() {
        return Integer.hashCode(productId.get());
    }
}
