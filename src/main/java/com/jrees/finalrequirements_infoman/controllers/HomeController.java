package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.Product;
import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
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
    private TextField calculatorDisplay;

    @FXML
    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    @FXML
    private Button buttonAdd, buttonSubtract, buttonMultiply, buttonDivide, buttonClear, buttonEquals;

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

        // Initialize the calculator's display
        calculatorDisplay.setText("");
    }

    // Method to load products from the database
    private void loadProductsFromDatabase() {
        String query = "SELECT * FROM products"; // Assuming 'products' is the table name

        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("name");
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

    // Calculator button actions
    @FXML
    public void handleButtonClick(MouseEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();

        if (buttonText.equals("C")) {
            calculatorDisplay.clear(); // Clear display
        } else if (buttonText.equals("=")) {
            // Evaluate the expression in the display (implement evaluation logic)
            try {
                String result = evaluateExpression(calculatorDisplay.getText());
                calculatorDisplay.setText(result);
            } catch (Exception e) {
                calculatorDisplay.setText("Error");
            }
        } else {
            // Append the clicked button text to the display
            calculatorDisplay.appendText(buttonText);
        }
    }

    // Sample method for simple expression evaluation (implement as needed)
    private String evaluateExpression(String expression) {
        try {
            double result = eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            return "Error";
        }
    }

    // Simple expression evaluator (you can use Java's script engine or another method here)
    private double eval(String expression) {
        try {
            return new Object() {
                public double evaluate(String expression) {
                    return Double.parseDouble(expression);
                }
            }.evaluate(expression);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @FXML
    public void close() {
        db.closeConnection(); // Close the database connection when the user exits the screen
    }
}
