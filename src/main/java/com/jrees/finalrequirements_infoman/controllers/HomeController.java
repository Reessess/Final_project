package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.Product;
import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class HomeController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Integer> productQuantityColumn;

    @FXML
    private TextField resultProductName;

    @FXML
    private TextField resultProductQuantity;  // Changed to Stock

    @FXML
    private TextField resultProductPrice;

    @FXML
    private TextField resultTotalPrice;

    @FXML
    private Button addProductButton, removeProductButton, searchButton;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private Database db;

    @FXML
    public void initialize() {
        // Set up database connection
        db = new Database();

        // Initialize the product table columns
        productIdColumn.setCellValueFactory(cellData -> cellData.getValue().getProductIdProperty().asObject());
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());
        productQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());

        // Load products from the database
        loadProductsFromDatabase();

        // Set the data to the table
        productTable.setItems(productList);
    }

    // Method to load products from the database
    private void loadProductsFromDatabase() {
        String query = "SELECT product_id, product_name, price, quantity FROM products";

        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double productPrice = rs.getDouble("price");
                int productQuantity = rs.getInt("quantity");

                // Add each product to the ObservableList
                productList.add(new Product(productId, productName, productPrice, productQuantity));
            }
        } catch (SQLException e) {
            System.err.println("Error loading products from database.");
            e.printStackTrace();
        }
    }

    // Method to handle product selection from the table
    @FXML
    public void handleProductSelection() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            resultProductName.setText(selectedProduct.getName());  // Using getName instead of getProductName
            resultProductPrice.setText(String.format("%.2f", selectedProduct.getPrice()));
            resultProductQuantity.setText(String.valueOf(selectedProduct.getQuantity()));  // Changed to show stock
            resultTotalPrice.clear();  // Clear total price when a new product is selected
        }
    }

    // Method to handle search functionality
    @FXML
    public void handleSearch() {
        String searchQuery = resultProductName.getText().trim();

        if (!searchQuery.isEmpty()) {
            for (Product product : productList) {
                if (product.getName().equalsIgnoreCase(searchQuery)) {
                    resultProductPrice.setText(String.format("%.2f", product.getPrice()));
                    resultProductQuantity.setText(String.valueOf(product.getQuantity()));  // Changed to stock
                    resultTotalPrice.clear();  // Clear total price when searching
                    return;
                }
            }
            // If no match is found, clear the fields
            resultProductPrice.clear();
            resultProductQuantity.clear();  // Changed to stock
            resultTotalPrice.clear();
            showAlert("Product Not Found", "No product found with the name: " + searchQuery);
        } else {
            showAlert("Empty Search", "Please enter a product name to search.");
        }
    }

    // Show an alert if the product is not found
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to calculate total price based on stock input (quantity remains valid in the backend)
    @FXML
    public void handleQuantityChange() {
        try {
            int stock = Integer.parseInt(resultProductQuantity.getText());  // Changed to stock
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

            if (selectedProduct != null && stock > 0) {
                double price = selectedProduct.getPrice();
                double totalPrice = price * stock;
                resultTotalPrice.setText(String.format("%.2f", totalPrice));  // Display total price
            } else {
                resultTotalPrice.clear();  // Clear if invalid stock
            }
        } catch (NumberFormatException e) {
            resultTotalPrice.clear();  // Clear if invalid input
        }
    }

    @FXML
    public void close() {
        db.closeConnection();  // Close the database connection when the user exits the screen
    }
}
