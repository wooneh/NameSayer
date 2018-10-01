package NameSayer.controller;

import NameSayer.task.SceneChanger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class InputNamesController {
	@FXML TextField courseCode;
	@FXML TextArea studentNames;
	@FXML Button practice;

	public void initialize() {
		practice.setOnAction(event -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/NameSayer/view/NameSayer.fxml"));
				practice.getScene().setRoot(loader.load());
				NameSayerController controller = loader.getController();
				controller.setPracticeNames(studentNames.getText().split("\n"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
