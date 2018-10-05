package NameSayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;

public class HelpWindowController {

    @FXML private Button backButton;

    @FXML
    private void backButtonAction(ActionEvent event) {
        try {
            backButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/HomeScreen.fxml")).load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
