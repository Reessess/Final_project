package com.jrees.finalrequirements_infoman.controllers;
import com.jrees.finalrequirements_infoman.models.CartItem;

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
import java.util.Optional;
import java.util.List;


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
    private Button addNewProductButton, removeProductFromTableButton;
    @FXML
    private TableView<CartItem> cartTableView;
    @FXML
    private TableColumn<CartItem, String> cartProductNameColumn;
    @FXML
    private TableColumn<CartItem, Integer> cartQuantityColumn;
    @FXML
    private TableColumn<CartItem, Double> cartPriceColumn;
    @FXML
    private TextField resultProductPrice;
    @FXML
    private TextField resultProductQuantity;
    @FXML
    private TextArea receiptTextArea;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField productStockField;
    @FXML
    private  TextField productPriceField;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField customerPhoneField;
    @FXML
    private TextField customerEmailField;
    @FXML
    private Button increaseQuantityButton;
    @FXML
    private Button decreaseQuantityButton;



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
        cartTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

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
            // Just print the error stack trace, no alert is shown now
            e.printStackTrace();
        }
    }


    @FXML
    public void handleAddProduct() {
        // Get values from the input fields
        String productName = productNameField.getText();
        String productPriceText = productPriceField.getText();
        String productQuantityText = productStockField.getText();

        // Check if all fields are filled
        if (productName.isEmpty() || productPriceText.isEmpty() || productQuantityText.isEmpty()) {
            showAlert("Invalid Input", "Please fill in all fields to add a product.");
            return;
        }

        try {
            // Convert the price and quantity values
            double price = Double.parseDouble(productPriceText);
            int quantity = Integer.parseInt(productQuantityText);

            // Check if the product already exists in the local product list
            for (Product product : productList) {
                if (product.getName().equalsIgnoreCase(productName)) {
                    // If the product already exists, show an alert and return
                    showAlert("Product Already Exists", "The product already exists in the list.");
                    return;
                }
            }

            // Create a Database instance
            Database db = new Database();

            // Insert the new product into the database and get the generated product_id
            int generatedProductId = db.addProduct(productName, price, quantity);

            if (generatedProductId != -1) {
                // Manually add the new product to the list after insertion, now with the correct product_id
                productList.add(new Product(generatedProductId, productName, price, quantity));

                // Optionally, you can reload products from the database to ensure the list is always current
                loadProductsFromDatabase();  // Reload the product list from the database

                // Show success message
                showAlert("Product Added", "Successfully added.");
            } else {
                showAlert("Error", "Failed to add product to the database.");
            }

        } catch (NumberFormatException e) {
            // Handle invalid input format for price or quantity
            showAlert("Invalid Input", "Price and quantity should be valid numbers.");
        } catch (Exception e) {
            // Catch any other unexpected errors
            showAlert("Error", "An unexpected error occurred while adding the product.");
            e.printStackTrace();
        }
    }



    // Remove product from database
    @FXML
    public void handleRemoveProduct() {
        // Get the selected product from the table view
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        // Check if a product is selected
        if (selectedProduct != null) {
            // Create a confirmation dialog for the deletion
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to remove this product?");
            confirmationAlert.setContentText("Product: " + selectedProduct.getName());  // Using regular getter getName()

            // Show the dialog and wait for user input
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            // Check if the user clicked 'OK' (Confirmation)
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Log the product ID and confirmation of removal action
                System.out.println("Attempting to remove product with ID: " + selectedProduct.getProductId());

                // Remove the product from the database
                boolean productRemoved = db.removeProduct(selectedProduct.getProductId());  // Using regular getter getProductId()

                // Log the result of the removal action
                if (productRemoved) {
                    // Remove from the observable list (productList)
                    productList.remove(selectedProduct);

                    // Optionally, refresh the UI if needed (e.g., reload products or update table view)
                    // productTable.refresh();

                    showAlert("Success", "Product removed successfully.");
                } else {
                    showAlert("Error", "Failed to remove the product.");
                }
            }
        } else {
            showAlert("Error", "No product selected.");
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
                        .findFirst()
                        .orElse(null);

                if (existingItem == null) {
                    // If the product is not already in the cart, create a new cart item
                    CartItem newItem = new CartItem(selectedProduct, 1); // Set initial quantity to 1
                    cartList.add(newItem);

                    // Decrease local stock of the product
                    selectedProduct.decreaseQuantity(-1); // Decrease stock locally by 1

                    // Deduct 1 stock from the database
                    Database db = new Database();
                    boolean success = db.updateProductStock(selectedProduct.getProductId(), 0); // Decrease stock by 1 in DB
                    db.closeConnection();

                    if (!success) {
                        showAlert("Database Error", "Failed to update stock in the database.");
                        // Rollback local stock change if DB update fails
                        selectedProduct.increaseQuantity(1); // Restore stock locally
                        // Remove the product from cart if stock update fails
                        cartList.remove(newItem);
                    }
                } else {
                    // If the product already exists in the cart, show a prompt
                    showAlert("Product Already in Cart", "This product is already in your cart.");
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
        increaseQuantityButton.setDisable(true); // Prevent multiple clicks

        try {
            CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
            if (selectedCartItem != null) {
                Product product = selectedCartItem.getProduct();
                int availableStock = product.getQuantity();

                // Check if there is enough stock to increase quantity
                if (availableStock > 0) {
                    // Increase the quantity in the cart
                    selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1);

                    // Decrease the local stock by 1
                    product.increaseQuantity(1);

                    // Update the stock in the database
                    Database db = new Database();
                    boolean success = db.updateProductStock(product.getProductId(), 0); // Decrease stock by 1 in DB
                    db.closeConnection();

                    if (!success) {
                        // Rollback changes if database update fails
                        product.increaseQuantity(1); // Restore stock
                        selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1); // Undo cart quantity
                        showAlert("Database Error", "Failed to update stock in the database.");
                    } else {
                        // Update the total price of the cart after quantity change
                        updateTotalPrice();
                        cartTableView.refresh();
                    }
                } else {
                    showAlert("Insufficient Stock", "There is not enough stock to increase the quantity.");
                }
            } else {
                showAlert("No Cart Item Selected", "Please select a cart item to increase quantity.");
            }
        } finally {
            increaseQuantityButton.setDisable(false); // Re-enable button
        }
    }

    // Decrease quantity of a cart item
    @FXML
    public void handleDecreaseQuantity() {
        decreaseQuantityButton.setDisable(true); // Prevent multiple clicks

        try {
            CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
            if (selectedCartItem != null && selectedCartItem.getQuantity() > 1) {
                Product product = selectedCartItem.getProduct();

                // Decrease the quantity in the cart
                selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1);

                // Decrease the local stock by 1
                product.decreaseQuantity(1);

                // Update the stock in the database
                Database db = new Database();
                boolean success = db.updateProductStock(product.getProductId(), 0); // Increase stock by 1 in DB (returning to stock)
                db.closeConnection();

                if (!success) {
                    // Rollback changes if database update fails
                    product.increaseQuantity(1); // Restore local stock
                    selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1); // Undo cart quantity change
                    showAlert("Database Error", "Failed to update stock in the database.");
                } else {
                    // Update the total price of the cart after quantity change
                    updateTotalPrice();
                    cartTableView.refresh();
                }
            } else if (selectedCartItem != null) {
                showAlert("Quantity Limit", "Quantity cannot be less than 1.");
            } else {
                showAlert("No Cart Item Selected", "Please select a cart item to decrease quantity.");
            }
        } finally {
            decreaseQuantityButton.setDisable(false); // Re-enable button
        }
    }

    // Real-time product search
    @FXML
    public void handleSearch() {
        String searchQuery = resultProductName.getText().trim().toLowerCase();

        // Filter the product list based on the search query
        ObservableList<Product> searchResults = FXCollections.observableArrayList(
                productList.filtered(product -> product.getName().toLowerCase().contains(searchQuery))
        );

        if (searchResults.isEmpty()) {
            // Show the alert when the search button is clicked and no products are found
            showAlert("No Products Found", "No products match the search query.");
            resultProductPrice.clear();
            resultProductQuantity.clear();
        } else {
            // Display the details of the first matching product
            Product firstMatch = searchResults.get(0);
            resultProductPrice.setText(String.format("%.2f", firstMatch.getPrice()));
            resultProductQuantity.setText(String.valueOf(firstMatch.getStock()));
        }

        // Update the product table with the filtered results
        productTable.setItems(searchResults);
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

            // Get customer details from input fields
            String customerName = customerNameField.getText().trim();
            String customerPhone = customerPhoneField.getText().trim();
            String customerEmail = customerEmailField.getText().trim();

            if (customerName.isEmpty() || customerPhone.isEmpty() || customerEmail.isEmpty()) {
                showAlert("Incomplete Details", "Please provide customer name, phone, and email.");
                return;
            }

            double change = cashGiven - totalPrice;
            changeField.setText(String.format("₱%.2f", change));

            // Generate receipt content with customer details
            String receiptContent = generateReceiptContent(customerName, customerPhone, customerEmail, totalPrice, cashGiven, change);

            // Save transaction and get transaction ID
            int transactionId = saveTransactionToDatabase(customerName, customerPhone, customerEmail, totalPrice);

            if (transactionId == -1) {
                showAlert("Database Error", "Failed to record the transaction.");
                return;
            }

            // Deduct stock and record sales items
            saveSalesItemsToDatabase(transactionId, cartList);

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


    public int saveTransactionToDatabase(String customerName, String customerPhone, String customerEmail, double totalPrice) {
        String query = "INSERT INTO transactions (customer_name, customer_phone, customer_email, total_price, transaction_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos_db", "FinalProject", "reesjhed");
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters for the query
            statement.setString(1, customerName);
            statement.setString(2, customerPhone);
            statement.setString(3, customerEmail);
            statement.setDouble(4, totalPrice);
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));  // Set the current timestamp

            // Execute the query to insert the transaction record
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Retrieve the auto-generated transaction ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);  // Return the generated transaction ID
                    }
                }
            } else {
                System.out.println("Failed to save the transaction.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while saving the transaction to the database.");
        }
        return -1;  // Return -1 if the transaction was not saved
    }

    private void saveSalesItemsToDatabase(int transactionId, List<CartItem> cartList) {
        String query = "INSERT INTO sales_item (sales_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)";
        Connection connection = null; // Declare connection outside of try block

        try {
            // Establish the connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos_db", "FinalProject", "reesjhed");

            // Turn off auto-commit for batch processing
            connection.setAutoCommit(false);

            // Prepare the statement
            try (PreparedStatement statement = connection.prepareStatement(query)) {

                // Loop through each cart item and insert it into the sales_item table
                for (CartItem item : cartList) {
                    statement.setInt(1, transactionId);       // Set the transaction ID
                    statement.setInt(2, item.getProduct().getProductId());  // Get productId from Product object
                    statement.setInt(3, item.getQuantity());   // Set the quantity of the product
                    statement.setDouble(4, item.getTotalPrice());  // Set the total price of the item

                    // Add the insert query to the batch
                    statement.addBatch(); // Add to batch for better performance
                }

                // Execute the batch insert
                int[] rowsAffected = statement.executeBatch();

                // Commit the transaction to the database
                connection.commit();

                if (rowsAffected.length > 0) {
                    System.out.println("Sales items saved successfully.");
                } else {
                    System.out.println("Failed to save sales items.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "An error occurred while saving sales items to the database.");
                // If there was an error, roll back the transaction
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while establishing a connection to the database.");
        } finally {
            // Ensure the connection is closed properly
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.setAutoCommit(true); // Restore default auto-commit behavior
                    connection.close(); // Close the connection
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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

    private String generateReceiptContent(String customerName, String customerPhone, String customerEmail, double totalPrice, double cashGiven, double change) {
        StringBuilder receiptContent = new StringBuilder();

        // Receipt header
        receiptContent.append("Receipt\n");
        receiptContent.append("========================================\n");
        receiptContent.append("Customer: ").append(customerName).append("\n");
        receiptContent.append("Phone: ").append(customerPhone).append("\n");
        receiptContent.append("Email: ").append(customerEmail).append("\n");
        receiptContent.append("========================================\n");

        // List of cart items
        for (CartItem item : cartList) {
            receiptContent.append(item.getProduct().getName())
                    .append(" x")
                    .append(item.getQuantity())
                    .append(" - ₱")
                    .append(String.format("%.2f", item.getTotalPrice())) // Ensuring price is formatted to 2 decimal places
                    .append("\n");
        }

        // Total, cash, and change
        receiptContent.append("\n========================================\n");
        receiptContent.append("Total: ₱").append(String.format("%.2f", totalPrice)).append("\n");
        receiptContent.append("Cash Given: ₱").append(String.format("%.2f", cashGiven)).append("\n");
        receiptContent.append("Change: ₱").append(String.format("%.2f", change)).append("\n");
        receiptContent.append("========================================\n");
        receiptContent.append("Thank you for shopping BOSS!\n");

        return receiptContent.toString();
    }
    private void saveReceiptToFile(String receiptContent) {
        // Ensure receipt directory exists
        File receiptDirectory = new File("receipts");
        if (!receiptDirectory.exists()) {
            receiptDirectory.mkdirs();  // Create directory if it doesn't exist
        }

        // Generate a unique filename using current time in milliseconds
        long timestamp = System.currentTimeMillis();  // Get current time in milliseconds
        File file = new File(receiptDirectory, "receipt_" + timestamp + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(receiptContent);  // Write the receipt content to the file
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

