package com.jrees.finalrequirements_infoman.controllers;

import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    void handleLogin(ActionEvent event) {
        String enteredUsername = username.getText();
        String enteredPassword = password.getText();

        // Validate input
        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username and password are required.");
            return;
        }

        // Authenticate user in a background thread
        new Thread(() -> {
            boolean isAuthenticated = authenticateUser(enteredUsername, enteredPassword);

            // Switch back to JavaFX Application Thread to update the UI
            javafx.application.Platform.runLater(() -> {
                if (isAuthenticated) {
                    openHomeScreen();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
                }
            });
        }).start();
    }

    private boolean authenticateUser(String username, String password) {
        Database db = new Database();
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // Check if password hash uses an old version of the salt (e.g., $2a$)
                if (BCrypt.checkpw(password, storedHash)) {
                    // If the password matches but the salt version is outdated, rehash and update the password in the database
                    if (storedHash.startsWith("$2a$")) {  // This checks if it uses the old salt version
                        String newHash = BCrypt.hashpw(password, BCrypt.gensalt());
                        updatePasswordInDatabase(username, newHash); // Update the password hash in the database
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
        }
        return false;
    }

    private void updatePasswordInDatabase(String username, String newHash) {
        String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection connection = new Database().getConnection();
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setString(1, newHash);
            ps.setString(2, username);
            ps.executeUpdate();
            System.out.println("Password rehashed and updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the password.");
        }
    }

    private void openHomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Home");
            stage.setScene(new Scene(root));
            stage.show();

            // Close login stage
            Stage currentStage = (Stage) username.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load the home screen.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
