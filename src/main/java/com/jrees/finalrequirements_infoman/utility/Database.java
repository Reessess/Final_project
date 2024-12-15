package com.jrees.finalrequirements_infoman.utility;
import com.jrees.finalrequirements_infoman.models.CartItem;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {
    private Connection connection;

    public Database() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Construct the connection string here in the Database class
            String dbUrl = "jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME;

            // Establish connection
            connection = DriverManager.getConnection(
                    dbUrl,  // Connection string is constructed directly here
                    Config.DB_USER,
                    Config.DB_PASSWORD
            );
            System.out.println("Connection to database successful!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing the database connection.");
            e.printStackTrace();
        }
    }

    public void generateUser(String username, String password) {
        if (userExists(username)) {
            System.out.println("User with username '" + username + "' already exists.");
            return;
        }

        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.executeUpdate();
            System.out.println("New user '" + username + "' created successfully.");
        } catch (SQLException e) {
            System.err.println("Error while creating user.");
            e.printStackTrace();
        }
    }

    public boolean userExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Return true if a record is found
        } catch (SQLException e) {
            System.err.println("Error while checking if user exists.");
            e.printStackTrace();
        }
        return false;
    }

    // Method to add a new product to the database
    public int addProduct(String productName, double price, int quantity) {
        String query = "INSERT INTO products (product_name, price, stock) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, productName);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);

            // Execute the update
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Retrieve the generated product_id
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);  // Return the generated product_id
                    }
                }
            }
        } catch (SQLException e) {
            // Handle SQLException
            System.err.println("Error while adding product: " + productName);
            e.printStackTrace();
        }
        return -1;  // Return an invalid id if something goes wrong
    }


    public boolean removeProduct(int productId) {
        // Check if the connection is null or closed, if not establish it
        if (connection == null) {
            try {
                // Re-establish the connection if it is null
                String dbUrl = "jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME;
                connection = DriverManager.getConnection(dbUrl, Config.DB_USER, Config.DB_PASSWORD);
                System.out.println("Reconnected to the database.");
            } catch (SQLException e) {
                System.err.println("Error reconnecting to the database.");
                e.printStackTrace();
                return false;  // If we cannot connect, return false
            }
        } else {
            // If the connection is not null, check if it is closed
            try {
                if (connection.isClosed()) {
                    // If closed, reconnect
                    String dbUrl = "jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME;
                    connection = DriverManager.getConnection(dbUrl, Config.DB_USER, Config.DB_PASSWORD);
                    System.out.println("Reconnected to the database.");
                }
            } catch (SQLException e) {
                System.err.println("Error checking if the database connection is closed.");
                e.printStackTrace();
                return false;
            }
        }

        // SQL query to delete the product by its ID
        String query = "DELETE FROM products WHERE product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);  // Set the product ID in the query

            // Execute the update and check if any rows were affected
            int rowsAffected = stmt.executeUpdate();

            // Log and check if any rows were affected (i.e., product was removed)
            if (rowsAffected > 0) {
                System.out.println("Product with ID " + productId + " removed successfully.");
                return true;  // Product deleted
            } else {
                System.out.println("No product found with ID " + productId);
                return false;  // No rows affected
            }
        } catch (SQLException e) {
            System.err.println("Error removing product with ID " + productId);
            e.printStackTrace();
            return false;
        } finally {
            closeConnection();  // Ensure the connection is always closed after the operation
        }
    }


    // Method to update the stock of a product
    public boolean updateProductStock(int productId, int quantityChange) {
        if (quantityChange > 0) {
            // Check current stock in the database before deducting
            String checkStockQuery = "SELECT stock FROM products WHERE product_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkStockQuery)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    System.out.println("Current stock for product ID " + productId + ": " + currentStock);

                    // Check if there's enough stock to deduct
                    if (currentStock < quantityChange) {
                        System.err.println("Not enough stock for product ID " + productId);
                        return false;  // Not enough stock to deduct
                    }
                } else {
                    System.err.println("Product ID " + productId + " not found.");
                    return false;  // Product not found in database
                }
            } catch (SQLException e) {
                System.err.println("Error checking stock for product ID: " + productId);
                e.printStackTrace();
                return false;
            }
        }

        // Update the stock in the database
        String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateStockQuery)) {
            stmt.setInt(1, quantityChange); // Positive for deduction, negative for addition
            stmt.setInt(2, productId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock updated for product ID: " + productId);  // Stock updated
                return true;  // Stock successfully updated
            } else {
                System.err.println("Failed to update stock for product ID: " + productId);
                return false;  // Failed to update stock in the database
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock for product ID: " + productId);
            e.printStackTrace();
            return false;
        }
    }

    // Method to record a sale in the sales table
    public void recordSale(int productId, int quantity, double totalPrice) {
        String insertSaleQuery = "INSERT INTO sales (product_id, quantity, total_price, sale_date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(insertSaleQuery)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, totalPrice);
            stmt.executeUpdate();
            System.out.println("Sale recorded for product ID: " + productId);
        } catch (SQLException e) {
            System.err.println("Error recording sale for product ID: " + productId);
            e.printStackTrace();
        }
    }

    public void recordSalesItems(int transactionId, List<CartItem> cartList) {
        String insertSalesItemQuery = "INSERT INTO sales_item (sales_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos_db", "FinalProject", "reesjhed");
             PreparedStatement stmt = connection.prepareStatement(insertSalesItemQuery)) {

            for (CartItem item : cartList) {
                stmt.setInt(1, transactionId);  // Set the transaction ID
                stmt.setInt(2, item.getProduct().getProductId());  // Get productId from Product object
                stmt.setInt(3, item.getQuantity());  // Set the quantity of the product
                stmt.setDouble(4, item.getTotalPrice());  // Set the total price of the item

                stmt.addBatch();  // Add the insert operation to the batch
            }

            int[] rowsAffected = stmt.executeBatch();
            if (rowsAffected.length > 0) {
                System.out.println("Sales items saved successfully.");
            } else {
                System.err.println("Failed to save sales items. No rows were affected.");
            }

        } catch (SQLException e) {
            System.err.println("Error occurred while saving sales items to the database. Details:");
            e.printStackTrace();
        }
    }


    // Method to process the checkout (update stock and record sales)
    public void processCheckout(int productId, int quantity, double totalPrice, int transactionId, List<CartItem> cartList) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Update product stock
            updateProductStock(productId, quantity);

            // Record sale
            recordSale(productId, quantity, totalPrice);

            // Record sales items
            recordSalesItems(transactionId, cartList);

            connection.commit(); // Commit transaction
            System.out.println("Checkout processed successfully.");
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback on error
                    System.err.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction.");
                ex.printStackTrace();
            }
            System.err.println("Error processing checkout.");
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true); // Restore default auto-commit behavior
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit.");
                e.printStackTrace();
            }
        }
    }
}

