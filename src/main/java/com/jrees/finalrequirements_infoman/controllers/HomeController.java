package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.CartItem;
import com.jrees.finalrequirements_infoman.models.Product;
import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

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
    private TextField resultProductQuantity;

    @FXML
    private TextField resultProductPrice;

    @FXML
    private TextField totalPriceField;

    @FXML
    private TextField calculatorTotalField;  // Added field to show total price in calculator

    @FXML
    private Button addProductButton, removeProductButton, searchButton, checkoutButton;

    @FXML
    private TableView<CartItem> cartTableView;

    @FXML
    private TableColumn<CartItem, String> cartProductNameColumn;

    @FXML
    private TableColumn<CartItem, Integer> cartQuantityColumn;

    @FXML
    private TableColumn<CartItem, Double> cartPriceColumn;

    @FXML
    private TextField cashField;

    @FXML
    private TextField changeField; // Field to display the change

    @FXML
    public void handleCalculateCash() {
        try {
            double cashGiven = Double.parseDouble(cashField.getText().trim());
            double totalPrice = Double.parseDouble(totalPriceField.getText().trim());

            if (cashGiven < totalPrice) {
                showAlert("Insufficient Cash", "The cash provided is less than the total price.");
            } else {
                double change = cashGiven - totalPrice;
                changeField.setText("₱" + String.format("%.2f", change));  // Show the change in the changeField
                cashField.clear();
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number in the cash field.");
        }
    }

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private Database db;

    @FXML
    public void initialize() {
        db = new Database();

        // Set up columns for products table
        productIdColumn.setCellValueFactory(cellData -> cellData.getValue().getProductIdProperty().asObject());
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());
        productQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());

        // Set up columns for cart table
        cartProductNameColumn.setCellValueFactory(cellData -> cellData.getValue().getProduct().getNameProperty());
        cartQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());
        cartPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalPriceProperty().asObject());

        loadProductsFromDatabase();
        productTable.setItems(productList);

        // Set cartList to the TableView
        cartTableView.setItems(cartList);

        // Add listener to resultProductName for real-time search updates
        resultProductName.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    // Load products from the database
    private void loadProductsFromDatabase() {
        String query = "SELECT product_id, product_name, price, stock FROM products";

        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double productPrice = rs.getDouble("price");
                int productQuantity = rs.getInt("stock");

                productList.add(new Product(productId, productName, productPrice, productQuantity));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading products from the database.");
            e.printStackTrace();
        }
    }

    // Add product to cart
    @FXML
    public void handleAddProductToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Check if product already exists in the cart
            for (CartItem item : cartList) {
                if (item.getProduct().equals(selectedProduct)) {
                    item.setQuantity(item.getQuantity() + 1);  // Increase quantity if already in cart
                    updateTotalPrice();  // Update the total price after adding
                    return;
                }
            }

            // Add new item to the cart
            CartItem cartItem = new CartItem(selectedProduct, 1);
            cartList.add(cartItem);
            updateTotalPrice();  // Update the total price after adding
        } else {
            showAlert("No Product Selected", "Please select a product to add to the cart.");
        }
    }

    // Remove product from cart
    @FXML
    public void handleRemoveFromCart() {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();

        if (selectedCartItem != null) {
            cartList.remove(selectedCartItem);  // Remove the item from the cart
            updateTotalPrice();  // Update the total price after removing
        } else {
            showAlert("No Cart Item Selected", "Please select a cart item to remove.");
        }
    }

    // Real-time search handler
    @FXML
    public void handleSearch() {
        String searchQuery = resultProductName.getText().trim().toLowerCase(); // Case-insensitive search

        if (!searchQuery.isEmpty()) {
            ObservableList<Product> searchResults = FXCollections.observableArrayList();

            // Search through all products and add those that match the search query
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(searchQuery)) {
                    searchResults.add(product);
                }
            }

            // If search results are found, update the table view with filtered products
            if (!searchResults.isEmpty()) {
                productTable.setItems(searchResults);  // Update the table view with search results
                resultProductPrice.setText(String.format("%.2f", searchResults.get(0).getPrice()));
                resultProductQuantity.setText(String.valueOf(searchResults.get(0).getQuantity()));
                totalPriceField.clear();  // Clear total price when searching
            } else {
                showAlert("Product Not Found", "No products found matching the search query.");
                productTable.setItems(FXCollections.observableArrayList()); // Clear table if no match
            }
        } else {
            // If search query is empty, reset to show all products
            productTable.setItems(productList);  // Reset table to show all products
            resultProductPrice.clear();
            resultProductQuantity.clear();
            totalPriceField.clear();
        }
    }

    // Update the total price field based on cart items
    private void updateTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : cartList) {
            totalPrice += item.getTotalPrice();  // Sum the total price of all items
        }
        totalPriceField.setText(String.format("%.2f", totalPrice));  // Update the total price field with the calculated value

        // Also update the calculator total field
        calculatorTotalField.setText(String.format("%.2f", totalPrice));  // Show total price in calculator
    }

    // Show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Close database connection
    @FXML
    public void close() {
        db.closeConnection();
    }

    // Increase quantity in cart
    @FXML
    public void handleIncreaseQuantity(ActionEvent event) {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1);  // Increase quantity
            updateTotalPrice();  // Update total price
        }
    }

    // Decrease quantity in cart
    @FXML
    public void handleDecreaseQuantity(ActionEvent event) {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null && selectedCartItem.getQuantity() > 1) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1);  // Decrease quantity
            updateTotalPrice();  // Update total price
        }
    }

    // Handle checkout
    @FXML
    public void handleCheckout() {
        // Step 1: Calculate the total price from the cart
        double totalPrice = 0;
        for (CartItem cartItem : cartList) {
            totalPrice += cartItem.getTotalPrice();  // Sum the total price of all items
        }

        // Step 2: Display the total price
        totalPriceField.setText(String.format("%.2f", totalPrice));  // Update the total price field

        // Step 3: Optionally, you can save this checkout data to a database or generate a receipt
        // For example, you can insert the checkout details into a transactions table

        // Step 4: Clear the cart after checkout
        cartList.clear();  // Clear the cart list
        cartTableView.setItems(cartList);  // Update the cart table view with the cleared cart

        // Optionally: Show a confirmation message
        showAlert("Checkout Complete", "Your checkout was successful. Thank you for your purchase!");

        // Step 5: Reset any fields
        totalPriceField.clear();  // Clear total price field
        calculatorTotalField.clear();  // Clear the calculator total price field
        resultProductName.clear();  // Clear search bar
        resultProductPrice.clear();  // Clear product details
        resultProductQuantity.clear();  // Clear quantity field
    }
}
