package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.utility.Config;
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
            // Establish connection
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME,
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
}
