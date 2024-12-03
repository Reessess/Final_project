package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.CartItem;
import com.jrees.finalrequirements_infoman.models.Product;
import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    private TextField resultProductName, resultProductQuantity, resultProductPrice, totalPriceField, calculatorTotalField, cashField, changeField;
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
    private TextField customerNameField;
    @FXML
    private TextArea receiptTextArea;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private Database db;

    @FXML
    public void initialize() {
        db = new Database();

        // Initialize product table columns
        productIdColumn.setCellValueFactory(cellData -> cellData.getValue().getProductIdProperty().asObject());
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());
        productQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());

        // Initialize cart table columns
        cartProductNameColumn.setCellValueFactory(cellData -> cellData.getValue().getProduct().getNameProperty());
        cartQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());
        cartPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalPriceProperty().asObject());

        loadProductsFromDatabase();
        productTable.setItems(productList);
        cartTableView.setItems(cartList);

        // Real-time search
        resultProductName.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    // Load products from database
    private void loadProductsFromDatabase() {
        String query = "SELECT product_id, product_name, price, stock FROM products";
        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
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
            // Check if there's enough stock to add to the cart
            int availableStock = selectedProduct.getQuantity();
            if (availableStock > 0) {
                // Decrease stock in the local product list
                selectedProduct.decreaseQuantity(1);

                // Check if the product is already in the cart
                CartItem existingItem = cartList.stream()
                        .filter(item -> item.getProduct().equals(selectedProduct))
                        .findFirst().orElse(null);

                if (existingItem != null) {
                    // If the product exists, increase the quantity in the cart
                    existingItem.setQuantity(existingItem.getQuantity() + 1);
                } else {
                    // If the product is not in the cart, create a new cart item
                    CartItem newItem = new CartItem(selectedProduct, 1);
                    cartList.add(newItem);
                }

                updateTotalPrice();  // Update the total price of the cart
            } else {
                showAlert("Out of Stock", "This product is out of stock.");
            }
        } else {
            showAlert("No Product Selected", "Please select a product to add to the cart.");
        }
    }


    // Remove product from cart
    @FXML
    public void handleRemoveFromCart() {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null) {
            cartList.remove(selectedCartItem);
            updateTotalPrice();
        } else {
            showAlert("No Cart Item Selected", "Please select a cart item to remove.");
        }
    }

    // Increase quantity of a cart item
    @FXML
    public void handleIncreaseQuantity() {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1);
            updateTotalPrice();
        } else {
            showAlert("No Cart Item Selected", "Please select a cart item to increase quantity.");
        }
    }

    // Decrease quantity of a cart item
    @FXML
    public void handleDecreaseQuantity() {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null && selectedCartItem.getQuantity() > 1) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1);
            updateTotalPrice();
        } else if (selectedCartItem != null) {
            showAlert("Quantity Limit", "Quantity cannot be less than 1.");
        } else {
            showAlert("No Cart Item Selected", "Please select a cart item to decrease quantity.");
        }
    }

    // Real-time product search
    @FXML
    public void handleSearch() {
        String searchQuery = resultProductName.getText().trim().toLowerCase();
        ObservableList<Product> searchResults = FXCollections.observableArrayList(
                productList.filtered(product -> product.getName().toLowerCase().contains(searchQuery))
        );

        productTable.setItems(searchResults);
        if (searchResults.isEmpty()) {
            showAlert("No Products Found", "No products match the search query.");
        }
    }

    // Update total price
    private void updateTotalPrice() {
        double totalPrice = cartList.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        totalPriceField.setText(String.format("%.2f", totalPrice));
        calculatorTotalField.setText(String.format("%.2f", totalPrice));
    }

    // Handle checkout
    // Handle checkout
    @FXML
    public void handleCheckout() {
        if (cartList.isEmpty()) {
            showAlert("Cart Empty", "No items in the cart to checkout.");
            return;
        }

        try {
            double cashGiven = Double.parseDouble(cashField.getText().trim());
            double totalPrice = Double.parseDouble(totalPriceField.getText().trim());

            if (cashGiven < totalPrice) {
                showAlert("Insufficient Cash", "The cash provided is less than the total price.");
                return;
            }

            double change = cashGiven - totalPrice;
            changeField.setText(String.format("₱%.2f", change));

            // Generate receipt content
            String receiptContent = generateReceiptContent(totalPrice, cashGiven, change);

            // Deduct stock and record sales
            updateStockAndRecordSales();  // Ensure stock is updated

            // Save the receipt to a file
            saveReceiptToFile(receiptContent);

            // Clear the cart and update the total price
            cartList.clear();
            updateTotalPrice();

            // Display receipt in TextArea (optional)
            if (receiptTextArea != null) {
                receiptTextArea.setText(receiptContent);
            }

            showAlert("Checkout Successful", "Transaction completed successfully.");
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for cash.");
        }
    }


    private void updateStockAndRecordSales() {
        Connection connection = null;
        try {
            connection = db.getConnection(); // Open the connection
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is closed.");
                return;
            }

            // Start transaction (optional, but good practice for multiple updates)
            connection.setAutoCommit(false);

            // Query for updating stock in the database
            String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
            try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery)) {

                // Loop through cart items and update stock in the database
                for (CartItem item : cartList) {
                    Product product = item.getProduct();
                    int quantity = item.getQuantity();

                    updateStockStmt.setInt(1, quantity);
                    updateStockStmt.setInt(2, product.getProductId());
                    updateStockStmt.executeUpdate();

                    // After updating the database, also update the stock in the local product list
                    for (Product p : productList) {
                        if (p.getProductId() == product.getProductId()) {
                            p.decreaseQuantity(quantity); // Update the local product stock
                            break;
                        }
                    }
                }
            }

            // Query for inserting sales records into the database
            String insertSalesQuery = "INSERT INTO sales (product_id, quantity, total_price, date) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement insertSalesStmt = connection.prepareStatement(insertSalesQuery)) {

                // Loop through cart items and insert sales records
                for (CartItem item : cartList) {
                    Product product = item.getProduct();
                    int quantity = item.getQuantity();
                    double totalPrice = item.getTotalPrice();

                    insertSalesStmt.setInt(1, product.getProductId());
                    insertSalesStmt.setInt(2, quantity);
                    insertSalesStmt.setDouble(3, totalPrice);
                    insertSalesStmt.executeUpdate();
                }
            }

            // Commit the transaction if everything is successful
            connection.commit();

        } catch (SQLException e) {
            // Rollback if any exception occurs during the transaction
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert("Database Error", "Error updating stock and recording sales.");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close(); // Close the connection after operation is complete
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private String generateReceiptContent(double totalPrice, double cashGiven, double change) {
        StringBuilder receiptContent = new StringBuilder();
        receiptContent.append("Receipt\n\n");
        for (CartItem item : cartList) {
            receiptContent.append(item.getProduct().getName())
                    .append(" x")
                    .append(item.getQuantity())
                    .append(" - ₱")
                    .append(item.getTotalPrice())
                    .append("\n");
        }
        receiptContent.append("\nTotal: ₱").append(totalPrice)
                .append("\nCash Given: ₱").append(cashGiven)
                .append("\nChange: ₱").append(change)
                .append("\n\nThank you for shopping with us!");
        return receiptContent.toString();
    }

    private void saveReceiptToFile(String receiptContent) {
        try {
            File file = new File("receipt.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(receiptContent);
            }
        } catch (IOException e) {
            showAlert("File Error", "Error saving receipt to file.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}