package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public class About {
	@FXML Button homeButton;

	public void initialize() {
		homeButton.setOnAction(event -> {
			try {
				homeButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/HomeScreen.fxml")).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
