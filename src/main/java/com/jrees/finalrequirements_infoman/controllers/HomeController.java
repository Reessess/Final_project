package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.CartItem;
import com.jrees.finalrequirements_infoman.models.Product;
import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private TextField resultProductQuantity; // Changed to Stock

    @FXML
    private TextField resultProductPrice;

    @FXML
    private TextField totalPriceField; // Renamed resultTotalPrice to totalPriceField

    @FXML
    private Button addProductButton, removeProductButton, searchButton;

    @FXML
    private TableView<CartItem> cartTableView;  // Change ListView to TableView

    @FXML
    private TableColumn<CartItem, String> cartProductNameColumn;

    @FXML
    private TableColumn<CartItem, Integer> cartQuantityColumn;

    @FXML
    private TableColumn<CartItem, Double> cartPriceColumn;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList(); // Cart items list
    private Database db;

    @FXML
    public void initialize() {
        db = new Database();

        productIdColumn.setCellValueFactory(cellData -> cellData.getValue().getProductIdProperty().asObject());
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());
        productQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());

        cartProductNameColumn.setCellValueFactory(cellData -> cellData.getValue().getProduct().getNameProperty());
        cartQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());
        cartPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalPriceProperty().asObject());

        loadProductsFromDatabase();
        productTable.setItems(productList);

        // Set cartList to the TableView
        cartTableView.setItems(cartList);
    }

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
            System.err.println("Error loading products from database.");
            e.printStackTrace();
        }
    }

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

    @FXML
    public void handleSearch() {
        String searchQuery = resultProductName.getText().trim();

        if (!searchQuery.isEmpty()) {
            for (Product product : productList) {
                if (product.getName().equalsIgnoreCase(searchQuery)) {
                    resultProductPrice.setText(String.format("%.2f", product.getPrice()));
                    resultProductQuantity.setText(String.valueOf(product.getQuantity()));
                    totalPriceField.clear();  // Clear the total price field when searching
                    return;
                }
            }

            resultProductPrice.clear();
            resultProductQuantity.clear();
            totalPriceField.clear();
            showAlert("Product Not Found", "No product found with the name: " + searchQuery);
        } else {
            showAlert("Empty Search", "Please enter a product name to search.");
        }
    }

    private void updateTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : cartList) {
            totalPrice += item.getTotalPrice();
        }
        totalPriceField.setText(String.format("%.2f", totalPrice));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void close() {
        db.closeConnection();
    }

    // Increase Quantity
    @FXML
    public void handleIncreaseQuantity(MouseEvent event) {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() + 1);  // Increase quantity
            updateTotalPrice();  // Update total price
        }
    }

    // Decrease Quantity
    @FXML
    public void handleDecreaseQuantity(MouseEvent event) {
        CartItem selectedCartItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedCartItem != null && selectedCartItem.getQuantity() > 1) {
            selectedCartItem.setQuantity(selectedCartItem.getQuantity() - 1);  // Decrease quantity
            updateTotalPrice();  // Update total price
        }
    }
}
