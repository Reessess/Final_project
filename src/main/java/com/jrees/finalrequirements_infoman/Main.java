package com.jrees.finalrequirements_infoman;

import com.jrees.finalrequirements_infoman.utility.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Database db;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize the database before launching the application
        db = new Database();

        // Check if the user "FinalProject" exists before creating it
        if (!db.userExists("FinalProject")) {
            db.generateUser("FinalProject", "reesjhed");
            System.out.println("Default user created: FinalProject");
        } else {
            System.out.println("User 'FinalProject' already exists.");
        }

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 420, 350);
        stage.setTitle("SEASIDE LOGIN");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Close the database connection when the application stops
        if (db != null) {
            db.closeConnection();
            System.out.println("Database connection closed.");
        }
        super.stop();
    }

    public static void main(String[] args) {
        // Launch the application
        launch(args);
    }
}
