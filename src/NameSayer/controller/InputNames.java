package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class InputNames {
	@FXML ComboBox<String> courseCode;
	@FXML TextArea studentNames;
	@FXML Button practice;

	public void initialize() {
		File classFolder = new File("classes");

		if (classFolder.exists() || classFolder.mkdir()) {
			File[] classNames = classFolder.listFiles();
			if (classNames != null) for (File className : classNames) courseCode.getItems().add(className.getName());
		}
		TextFields.bindAutoCompletion(courseCode.getEditor(), courseCode.getItems()).setPrefWidth(courseCode.getPrefWidth());

		courseCode.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
			if (courseCode.getItems().contains(newValue)) { // check if the selected class already exists
				try { // read the list of names for the class in the class's folder
					File classList = new File("classes/" + newValue + "/" + newValue + ".txt");
					if (classList.exists()) studentNames.setText(String.join(System.lineSeparator(),Files.readAllLines(classList.toPath(), StandardCharsets.UTF_8)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else studentNames.clear();
		}));

		practice.setOnAction(event -> { // sends the input values to the main controller
			String selectedCourseCode = courseCode.getValue();
			if (selectedCourseCode != null && !selectedCourseCode.trim().isEmpty() && !studentNames.getText().trim().isEmpty() && selectedCourseCode.matches("[a-zA-Z0-9 _-]*")) {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/NameSayer/view/NameSayer.fxml"));
					practice.getScene().setRoot(loader.load());
					NameSayer controller = loader.getController();
					controller.setCourseCode(selectedCourseCode.trim());
					controller.setPracticeNames(Arrays.asList(studentNames.getText().split("\n")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Alert emptyAlert = new Alert(Alert.AlertType.WARNING);
				emptyAlert.setHeaderText("Invalid Course Code And Student Names Cannot Be Empty");
				emptyAlert.setContentText("Please enter a valid course code and at least one student name.");
				emptyAlert.showAndWait();
			}
		});
	}
}
