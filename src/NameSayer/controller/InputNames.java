package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import static NameSayer.Main.*;

/**
 * Controller for Input Class mode. Allows the user to input a list of names.
 */
public class InputNames {
	@FXML ComboBox<String> courseCode;
	@FXML TextArea studentNames;
	@FXML Button practice;
	@FXML Button uploadNames;
	@FXML Button Home;
	@FXML Button Help;

	/**
	 * This method adds the contents of a file containing names to the text area.
	 * @param file text containing the list of names.
	 */
	private void importNames(File file) {
		try { // read the list of names for the class in the class's folder
			if (file != null) studentNames.setText(String.join("\n", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialize() {
		File[] classNames = new File(CLASSES).listFiles(); // lists past classes
		if (classNames != null) {
			for (File className : classNames) { // exclude single name folder
				if (!className.getName().equals("Single Name")) courseCode.getItems().add(className.getName());
			}
		}
		TextFields.bindAutoCompletion(courseCode.getEditor(), courseCode.getItems()).setPrefWidth(courseCode.getPrefWidth());

		courseCode.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
			if (courseCode.getItems().contains(newValue)) { // check if the selected class already exists
				importNames(new File(CLASSES + "/" + newValue + "/" + newValue + ".txt"));
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
					controller.setPracticeNames(new ArrayList<>(Arrays.asList(studentNames.getText().split("\n"))));
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

		uploadNames.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Upload Text File");
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
			fileChooser.getExtensionFilters().add(filter);
			importNames(fileChooser.showOpenDialog(new Stage()));
		});

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
