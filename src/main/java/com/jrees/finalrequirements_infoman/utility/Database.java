package com.jrees.finalrequirements_infoman.utility;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    // Method to update the stock of a product
    public void updateProductStock(int productId, int quantity) {
        // Check current stock for debugging purposes
        String checkStockQuery = "SELECT stock FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkStockQuery)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                System.out.println("Current stock for product ID " + productId + ": " + currentStock);
                if (currentStock < quantity) {
                    System.err.println("Not enough stock for product ID " + productId);
                    return;
                }
            } else {
                System.err.println("Product ID " + productId + " not found.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock for product ID: " + productId);
            e.printStackTrace();
        }

        String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateStockQuery)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock updated for product ID: " + productId);
            } else {
                System.err.println("Failed to update stock for product ID: " + productId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock for product ID: " + productId);
            e.printStackTrace();
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

    // Method to process the checkout (update stock and record sales)
    public void processCheckout(int productId, int quantity, double totalPrice) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Update product stock
            updateProductStock(productId, quantity);

            // Record sale
            recordSale(productId, quantity, totalPrice);

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
