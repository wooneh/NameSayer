package NameSayer.controller;

import NameSayer.*;
import NameSayer.task.*;
import NameSayer.task.Timer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.applet.Applet;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import static NameSayer.Main.*;

/**
 * Controller for the main Practice and Recording modules. Allows the user to practice and record names.
 */
public class NameSayer {
	@FXML VBox leftColumn;
	@FXML Text currentCourse;
	@FXML Text completion;
	@FXML Text numCreations;
	@FXML TableView<Creation> Creations;
	@FXML Button helpButton;
	@FXML Button homeButton;
	@FXML Text info;
	@FXML TextFlow currentCreationName;
	@FXML HBox mediaButtons;
	@FXML Button prevCreation;
	@FXML Button playButton;
	@FXML Button nextCreation;
	@FXML Button refreshCreation;
	@FXML ComboBox<Version> nameParts;
	@FXML CheckBox badRating;
	@FXML HBox rateRecording;
	@FXML Button attemptName;
	@FXML ComboBox<Attempt> pastAttempts;
	@FXML Button playAttempt;
	@FXML Button trashAttempt;
	@FXML Button saveAttempt;
	@FXML HBox recordingIndicators;
	@FXML ProgressBar soundLevelBar;
	@FXML ProgressBar countDown;
	@FXML Button showHideButton;

	/**
	 * This method adds valid names to the practice list.
	 * @param practiceNames The list of names to practice
	 */
	public void setPracticeNames(List<String> practiceNames) {
		practiceNames.forEach(name -> name = name.trim()); // trim whitespace

		if (practiceNames.removeIf(name -> name.isEmpty() || !name.matches("[a-zA-Z0-9 -]*"))) {
			Alert importNames = new Alert(Alert.AlertType.WARNING); // show an alert if the list was filtered
			importNames.setHeaderText("Error adding names");
			importNames.setContentText("Some names were not added as they contained invalid characters.");
			importNames.showAndWait();
		}

		practiceNames = practiceNames.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

		try { // course code mus be set beforehand
			File classList = new File(CLASSES + "/" + currentCourse.getText() + "/" + currentCourse.getText() + ".txt");
			Files.write(classList.toPath(), practiceNames, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.setItems(FXCollections.observableArrayList(practiceNames.stream().map(name -> new Creation(name)).collect(Collectors.toList())));
		completion.textProperty().bind(Creation.getNumCreationsThatHaveAttempts().asString()); // display number of creations that have been attempted
		numCreations.textProperty().bind(new SimpleIntegerProperty(Creations.getItems().size()).asString());

		File[] attemptFiles = new File(CLASSES + "/" + currentCourse.getText()).listFiles(); // set attempts for each creation
		if (attemptFiles != null) for (File attemptFile : attemptFiles) new Creation(attemptFile.getName(), currentCourse.getText());
		Creations.getSelectionModel().selectFirst();
	}

	/**
	 * This method creates a folder (if one doesn't exist) for the inputted class
	 * @param courseCode Course code for the selected class
	 */
	public void setCourseCode(String courseCode) {
		File classFolder = new File(CLASSES + "/" + courseCode);
		if (classFolder.exists() || classFolder.mkdir()) currentCourse.setText(courseCode);
	}

	public void initialize() {
		Name.setAllNames();
		Rating.setBadRatings();
		MicrophoneLevel microphoneLevel = new MicrophoneLevel();
		Thread micThread = new Thread(microphoneLevel.setProgressBar(soundLevelBar));
		micThread.setDaemon(true); // close this thread when application is closed
		micThread.start();

		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			nameParts.getItems().clear();
			saveAttempt.setDisable(true);
			if (newValue != null) {
				pastAttempts.setItems(FXCollections.observableArrayList(newValue.getAttempts()));
				pastAttempts.getSelectionModel().selectFirst();

				List<Text> displayCreationName = new ArrayList<>();
				List<String> filesToConcatenate = new ArrayList<>();
				info.setText("");
				for (String namePart : newValue.getNameParts()) { // Associates a name part with a name from the database
					Text namePartText = new Text(namePart + " ");
					namePartText.setStyle("-fx-font-size: 32px;");

					if (Name.getAllNames().containsKey(namePart.toLowerCase())) { // find a recording for the name part
						PriorityQueue<Version> nameVersions = Name.getAllNames().get(namePart.toLowerCase()).getVersions();
						filesToConcatenate.add("file '../" + NAMES_CORPUS + "/" + nameVersions.peek().getFileName() + "'");
						nameParts.getItems().add(nameVersions.peek());
					} else {
						namePartText.setStrikethrough(true); // cross out name part if recording does not exist in database
						info.setText("Some names are not available.");
					}
					displayCreationName.add(namePartText);
				}
				currentCreationName.getChildren().setAll(displayCreationName); // Shows the name of the current creation in the UI

				if (!nameParts.getItems().isEmpty()) { // concatenate the chosen audio files
					playButton.setDisable(false);
					nameParts.getSelectionModel().selectFirst();

					try { // writes the files to concatenate to a text file
						Files.write(new File(TEMP + "/concatenatedFiles.txt").toPath(), filesToConcatenate, StandardCharsets.UTF_8);
					} catch (IOException e) {
						e.printStackTrace();
					}

					leftColumn.setDisable(true);
					mediaButtons.setDisable(true);
					String saveInfo = info.getText();
					info.setText("Loading...");
					Concatenate concatenate = new Concatenate();
					concatenate.setOnSucceeded(finished -> {
						info.setText(saveInfo);
						leftColumn.setDisable(false);
						mediaButtons.setDisable(false);
					});
					new Thread(concatenate).start();
				} else playButton.setDisable(true);
			} else {
				Text chooseName = new Text("Choose Name");
				chooseName.setStyle("-fx-font-size: 32px;");
				currentCreationName.getChildren().setAll(chooseName);
				pastAttempts.getItems().clear();
			}
		});

		nameParts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // update rating
				rateRecording.setDisable(false);
				if (Rating.getBadRatings().contains(newValue.getFileName())) badRating.setSelected(true);
				else badRating.setSelected(false);
			} else {
				rateRecording.setDisable(true);
				badRating.setSelected(false);
			}
		});

		playButton.setOnAction(event -> { // plays the selected audio.
			try {
				Applet.newAudioClip(new File(TEMP + "/normalized.wav").toURI().toURL()).play();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});

		nextCreation.setOnAction(event -> { // Loads the next queued creation. Cycles to the beginning at the end.
			if (Creations.getSelectionModel().isSelected(Creations.getItems().size() - 1)) Creations.getSelectionModel().selectFirst();
			else Creations.getSelectionModel().selectNext();
			Creations.scrollTo(Creations.getSelectionModel().getSelectedItem());
		});

		prevCreation.setOnAction(event -> { // Loads the previously queued creation. Cycles to the end at at the beginning.
			if (Creations.getSelectionModel().isSelected(0)) Creations.getSelectionModel().selectLast();
			else Creations.getSelectionModel().selectPrevious();
			Creations.scrollTo(Creations.getSelectionModel().getSelectedItem());
		});

		refreshCreation.setOnAction(event -> { // If the user rates a recording as bad, they can choose to refresh the name with new recordings
			Creation selectedCreation = Creations.getSelectionModel().getSelectedItem();
			Creations.getSelectionModel().clearSelection();
			Creations.getSelectionModel().select(selectedCreation);
		});

		attemptName.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();
			List<Region> regions = new ArrayList<>();
			regions.add(attemptName);
			regions.add(leftColumn);
			regions.add(mediaButtons);
			regions.add(saveAttempt);

			if (creation != null) {
				pastAttempts.setItems(FXCollections.observableArrayList(creation.getAttempts())); //clear past unsaved
				regions.forEach(region -> region.setDisable(true));
				soundLevelBar.setVisible(true);
				recordingIndicators.setVisible(true);
				new Thread(new Timer(countDown)).start();

				RecordAudio recording = new RecordAudio();
				recording.setOnSucceeded(finished -> { // user can choose to play, save, or delete the recording.
					pastAttempts.getItems().add(new Attempt(TEMP + "/UnsavedAttempt.wav"));
					pastAttempts.getSelectionModel().selectLast();
					regions.forEach(region -> region.setDisable(false));
					recordingIndicators.setVisible(false);
					soundLevelBar.setVisible(false);
				});

				new Thread(recording).start(); // starts recording the user's voice for 5 seconds.
			}
		});

		playAttempt.setOnAction(event -> {
			if (!pastAttempts.getSelectionModel().isEmpty()) pastAttempts.getSelectionModel().getSelectedItem().getClip().play();
		});

		trashAttempt.setOnAction(event -> {
			if (!pastAttempts.getSelectionModel().isEmpty()) {
				File selectedFile = pastAttempts.getSelectionModel().getSelectedItem().getFile();
				if (selectedFile.delete()) { // remove the attempt from the creation and update the dropdown
					if (!selectedFile.getName().equals("UnsavedAttempt.wav")) Creations.getSelectionModel().getSelectedItem().removeAttempt(pastAttempts.getSelectionModel().getSelectedItem());
					else saveAttempt.setDisable(true);
					pastAttempts.setItems(FXCollections.observableArrayList(Creations.getSelectionModel().getSelectedItem().getAttempts()));
					pastAttempts.getSelectionModel().selectFirst();
				}
			}
		});

		saveAttempt.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();
			SaveAudio saveAudio = new SaveAudio(currentCourse.getText(), creation.getName());
			saveAudio.setOnSucceeded(finished -> {
				creation.addAttempt(saveAudio.getValue());
				pastAttempts.setItems(FXCollections.observableArrayList(creation.getAttempts()));
				pastAttempts.getSelectionModel().selectLast();
				saveAttempt.setDisable(true);
			});

			new Thread(saveAudio).start();
		});

		completion.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(numCreations.getText())) { // display a reward when the user finishes all the names
				Alert reward = new Alert(Alert.AlertType.INFORMATION);
				reward.setHeaderText("Congratulations!");
				reward.setContentText("You have attempted all the names. Give yourself a pat on the back.");
				reward.showAndWait();
			}
		});

		badRating.selectedProperty().addListener((observable, oldValue, newValue) -> {
			Version version = nameParts.getSelectionModel().getSelectedItem();
			if (version != null) {
				if (newValue && !Rating.getBadRatings().contains(version.getFileName())) version.setRating(Rating.BAD);
				else if (!newValue && Rating.getBadRatings().contains(version.getFileName())) version.setRating(Rating.GOOD);
			}
		});

		showHideButton.setOnAction(event -> soundLevelBar.setVisible(!soundLevelBar.isVisible()));

		helpButton.setOnAction(event -> {
			try {
				Stage stage = new Stage();
				stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/Help.fxml"))));
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		homeButton.setOnAction(event -> {
			try {
				microphoneLevel.setCapturing(false);
				Creation.clearAlLCreations(); // I'm sorry for misusing static
				Name.clearAllNames();
				numCreations.textProperty().unbind(); // stop previous scenes from being updated
				completion.textProperty().unbind();
				homeButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/HomeScreen.fxml")).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
    }
}