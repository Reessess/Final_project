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

    // Method to update stock quantity in memory and in the database
    public boolean updateQuantity(int quantityToAddToCart) {
        // Get the current stock
        int currentStock = this.quantity.get();

        // Check if there is enough stock to add to the cart
        if (currentStock <= 0 || currentStock < quantityToAddToCart) {
            System.out.println("Insufficient stock for Product ID: " + productId.get());
            return false;  // Not enough stock to add to the cart
        }

        // Calculate the new stock after deducting from available stock
        int newStock = currentStock - quantityToAddToCart;

        // Update the local quantity state
        this.quantity.set(newStock);

        // Initialize Database and update the stock in the database (deducting stock)
        Database db = new Database();
        boolean success = db.updateProductStock(this.productId.get(), quantityToAddToCart);  // Deduct stock from database when adding to cart
        db.closeConnection();

        if (success) {
            System.out.println("Stock updated successfully in database for Product ID: " + productId.get());
        } else {
            System.out.println("Failed to update stock in database for Product ID: " + productId.get());
        }

        return success;  // Return success or failure of the database update
    }

    // Method to decrease stock when added to cart
    public boolean decreaseQuantity(int quantityToDecrease) {
        return updateQuantity(-quantityToDecrease);
    }

    // Method to increase stock when removing from cart
    public void increaseQuantity(int quantityToIncrease) {
        updateQuantity(quantityToIncrease);
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
