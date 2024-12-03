package com.jrees.finalrequirements_infoman;

import com.jrees.finalrequirements_infoman.controllers.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 420, 350);
        stage.setTitle("SEASIDE LOGIN");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Initialize the database and create a default user
        Database db = new Database();

        // Check if the user "FinalProject" exists before creating it
        if (!db.userExists("FinalProject")) {
            db.generateUser("FinalProject", "reesjhed");
            System.out.println("Default user created: FinalProject");
        } else {
            System.out.println("User 'FinalProject' already exists.");
        }

        launch();
    }
}
