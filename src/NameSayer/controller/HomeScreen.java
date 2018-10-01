package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public class HomeScreen {
	@FXML Button startButton;

	public void initialize() {
    	startButton.setOnAction(event -> {
    		try {
				startButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/InputNames.fxml")).load());
			} catch (IOException e) {
    			e.printStackTrace();
			}
    	});
	}
}
