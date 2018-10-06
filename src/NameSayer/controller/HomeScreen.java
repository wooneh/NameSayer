package NameSayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeScreen implements Initializable {
	@FXML Button startButton;

	@FXML
	ImageView imageView;

	@FXML
	public void helpButtonAction(ActionEvent event) {
		try {
			startButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/HelpWindow.fxml")).load());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		File file = new File("SOFTENG-206-Assignment-3/src/study.jpg");
		System.out.println(file);
		Image image = new Image(file.toURI().toString());
		imageView.setImage(image);

		startButton.setOnAction(event -> {
			try {
				startButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/InputNames.fxml")).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}