package com.jrees.finalrequirements_infoman.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    public void initialize() {
        welcomeLabel.setText("Welcome to the Home Screen!");
    }
}
