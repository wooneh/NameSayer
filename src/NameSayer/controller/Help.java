package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Help {
    @FXML private Button close;

    public void initialize(){
		close.setOnAction(event -> {
			Stage stage = (Stage) close.getScene().getWindow();
			stage.close();
		});
	}
}
