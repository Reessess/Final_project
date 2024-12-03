package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HomeController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TextField calculatorDisplay;

    @FXML
    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    @FXML
    private Button buttonAdd, buttonSubtract, buttonMultiply, buttonDivide, buttonClear, buttonEquals;

    // Observable list for products
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize product table with columns
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());

        // Sample data for products
        productList.add(new Product("Apple", 1.0));
        productList.add(new Product("Banana", 0.5));
        productList.add(new Product("Orange", 0.75));

        // Set the data to the table
        productTable.setItems(productList);

        // Initialize the calculator's display
        calculatorDisplay.setText("");
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
        // For example, use a script engine or custom logic to evaluate the expression
        return String.valueOf(eval(expression)); // Simple implementation
    }

    // Simple expression evaluator (you can use Java's script engine or another method here)
    private double eval(String expression) {
        // This is a basic implementation, more complex eval logic is possible
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
}
