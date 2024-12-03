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
    private TextField resultProductName, totalPriceField, calculatorTotalField, cashField, changeField;
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
            // Get available stock for the selected product
            int availableStock = selectedProduct.getQuantity();

            if (availableStock > 0) { // Check if there's stock available
                // Check if the product is already in the cart
                CartItem existingItem = cartList.stream()
                        .filter(item -> item.getProduct().equals(selectedProduct))
                        .findFirst().orElse(null);

                if (existingItem != null) {
                    // If the product is already in the cart, increase its quantity in the cart
                    existingItem.setQuantity(existingItem.getQuantity() + 1);

                    // Decrease local stock of the product and update database
                    boolean success = selectedProduct.decreaseQuantity(1); // Decrease stock locally and in DB
                    if (!success) {
                        showAlert("Database Error", "Failed to update stock in the database.");
                    }

                } else {
                    // If the product is not in the cart, create a new cart item
                    CartItem newItem = new CartItem(selectedProduct, 1);
                    cartList.add(newItem);

                    // Decrease local stock of the product and update database
                    boolean success = selectedProduct.decreaseQuantity(-1); // Decrease stock locally and in DB
                    if (!success) {
                        showAlert("Database Error", "Failed to update stock in the database.");
                    }
                }
                // Update the total price of the cart
                updateTotalPrice();
            } else {
                // If no stock available, show an alert
                showAlert("Out of Stock", "This product is out of stock.");
            }
        } else {
            // If no product is selected, show an alert
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
            Product product = selectedCartItem.getProduct();
            int availableStock = product.getQuantity();

            // Check if there's enough stock to increase quantity
            if (availableStock >= 1) {
                // Increase the quantity in the cart
                selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1);

                // Update the local stock by decreasing the available stock
                product.decreaseQuantity(1);

                updateTotalPrice(); // Update the total price of the cart
            } else {
                showAlert("Insufficient Stock", "There is not enough stock to increase the quantity.");
            }
        } else {
            showAlert("No Cart Item Selected", "Please select a cart item to increase quantity.");
        }
    }



    // Decrease quantity of a cart item
    @FXML
    public void handleDecreaseQuantity() {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null && selectedCartItem.getQuantity() > 1) {
            Product product = selectedCartItem.getProduct();

            // Decrease the quantity in the cart
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1);

            // Update the local stock by increasing the available stock
            product.increaseQuantity(1);

            updateTotalPrice();  // Update the total price of the cart
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

            // Start transaction
            connection.setAutoCommit(false);

            // Query for updating stock in the database
            String fetchStockQuery = "SELECT stock FROM products WHERE product_id = ?";
            String updateStockQuery = "UPDATE products SET stock = ? WHERE product_id = ?";
            try (PreparedStatement fetchStockStmt = connection.prepareStatement(fetchStockQuery);
                 PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery)) {

                // Loop through cart items and update stock in the database
                for (CartItem item : cartList) {
                    Product product = item.getProduct();
                    int cartQuantity = item.getQuantity();

                    // Fetch the current stock from the database
                    fetchStockStmt.setInt(1, product.getProductId());
                    try (ResultSet rs = fetchStockStmt.executeQuery()) {
                        if (rs.next()) {
                            int currentStock = rs.getInt("stock");

                            // Calculate the new stock
                            int newStock = currentStock - cartQuantity;
                            if (newStock < 0) {
                                throw new SQLException("Insufficient stock for product: " + product.getName());
                            }

                            // Update the stock in the database
                            updateStockStmt.setInt(1, newStock);
                            updateStockStmt.setInt(2, product.getProductId());
                            updateStockStmt.executeUpdate();

                            // Update the local product list with the new stock value
                            product.setQuantity(newStock);
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
