package NameSayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static NameSayer.Main.CLASSES;

/**
 * Controller for single input mode. Used when the user only wishes to practice a single name.
 */
public class SingleInput {
	@FXML TextField studentName;
	@FXML Button practice;
	@FXML Button Home;
	@FXML Button Help;

	private void clearSingleInputFolder() throws IOException {
		File singleInputFolder = new File(CLASSES + "/Single Name");
		File[] contents = singleInputFolder.listFiles();
		if (contents != null) for (File file : contents) if (!file.delete()) throw new IOException("Error clearing folder.");
	}

	public void initialize(){
		try {
			clearSingleInputFolder();
		} catch (IOException e) {
			e.printStackTrace();
		}

		studentName.setOnAction(event -> {
			if (!studentName.getText().trim().isEmpty() && studentName.getText().matches("[a-zA-Z -']*")) {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/NameSayer/view/NameSayer.fxml"));
					practice.getScene().setRoot(loader.load());
					NameSayer controller = loader.getController();

					List<String> name = new ArrayList<>();
					name.add(studentName.getText());
					controller.setCourseCode("Single Name");
					controller.setPracticeNames(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Alert emptyAlert = new Alert(Alert.AlertType.WARNING);
				emptyAlert.setHeaderText("Entered name may be empty or contain invalid characters.");
				emptyAlert.setContentText("Please enter a valid name.");
				emptyAlert.showAndWait();
			}
		});

		practice.setOnAction(event -> studentName.fireEvent(new ActionEvent()));

		Home.setOnAction(event -> {
			try {
				Home.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/HomeScreen.fxml")).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		Help.setOnAction(event -> {
			try {
				Stage stage = new Stage();
				stage.setTitle("User Manual");
				stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/Help.fxml"))));
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
