package NameSayer.controller;

import NameSayer.task.SceneChanger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class HomeScreenController {
	@FXML Button startButton;

	public void initialize() {
    	startButton.setOnAction(event -> new Thread(new SceneChanger(startButton, new FXMLLoader(getClass().getResource("/NameSayer/view/InputNames.fxml")))).start());
	}
}
