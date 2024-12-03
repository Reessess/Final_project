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

    public int getProductId() {
        return productId.get();
    }

    public String getName() {
        return name.get();  // This method returns the product name
    }

    public double getPrice() {
        return price.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

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
}
