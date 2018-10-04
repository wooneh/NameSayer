package NameSayer.controller;

import NameSayer.*;
import NameSayer.task.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javax.sound.sampled.*;
import java.applet.Applet;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import static NameSayer.Main.*;

public class NameSayer {
	@FXML VBox body;
	@FXML TableView<Creation> Creations;
	@FXML Button playButton;
	@FXML Button nextCreation;
	@FXML Button prevCreation;
	@FXML Button attemptName;
	@FXML Button playAttempt;
	@FXML Button trashAttempt;
	@FXML ComboBox<Version> nameParts;
	@FXML TextFlow currentCreationName;
	@FXML Text info;
	@FXML CheckBox badRating;
	@FXML HBox rateRecording;
	@FXML Text currentCourse;
	@FXML Text completion;
	@FXML Text numCreations;
	@FXML ComboBox<Attempt> pastAttempts;
	@FXML ProgressBar soundLevelBar;
	@FXML Button showHideButton;

	private SourceDataLine sourceLine;
	private TargetDataLine targetLine;
	private Thread micThread = new Thread(new Background());

	/**
	 * This method adds valid names to the practice list.
	 * @param practiceNames The list of names to practice
	 */
	public void setPracticeNames(List<String> practiceNames) {
		practiceNames.forEach(name -> name = name.trim()); // trim whitespace
		practiceNames.removeIf(name -> name.isEmpty() || !name.matches("[a-zA-Z0-9 -]*")); // remove invalid names
		practiceNames = practiceNames.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

		try { // course code must be set beforehand
			File classList = new File(CLASSES + "/" + currentCourse.getText() + "/" + currentCourse.getText() + ".txt");
			Files.write(classList.toPath(), practiceNames, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.setItems(FXCollections.observableArrayList(practiceNames.stream().map(name -> new Creation(name)).collect(Collectors.toList())));
		Creations.getSelectionModel().selectFirst();

		completion.textProperty().bind(Creation.getNumCreationsThatHaveAttempts().asString()); // display number of creations that have been attempted
		numCreations.textProperty().bind(new SimpleIntegerProperty(Creations.getItems().size()).asString());

		File[] attemptFiles = new File(CLASSES + "/" + currentCourse.getText()).listFiles(); // set attempts for each creation
		if (attemptFiles != null) for (File attemptFile : attemptFiles) new Creation(attemptFile.getName(), currentCourse.getText());
	}

	/**
	 * This method creates a folder (if one doesn't exist) for the inputted class
	 * @param courseCode Course code for the selected class
	 */
	public void setCourseCode(String courseCode) {
		File classFolder = new File(CLASSES + "/" + courseCode);
		if (classFolder.exists() || classFolder.mkdir()) currentCourse.setText(courseCode);
	}

	// TODO: Choose recordings for name parts that are not bad quality
	// TODO: Option to stop recording
	// TODO: User help tooltips
	// TODO: Single name input
	public void initialize() {
		File[] nameAudioFiles = NAMES_CORPUS.listFiles(); // folder containing database
		if (nameAudioFiles != null) for (File nameAudioFile : nameAudioFiles) new Name(nameAudioFile.getName()); // create database

		try { // loads the ratings for each recording
			if (BAD_RATINGS.exists() || BAD_RATINGS.createNewFile()) Rating.setBadRatings((Files.readAllLines(BAD_RATINGS.toPath(), StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			nameParts.getItems().clear();
			if (newValue != null) {
				pastAttempts.setItems(FXCollections.observableArrayList(newValue.getAttempts()));
				if (!pastAttempts.getItems().isEmpty()) pastAttempts.getSelectionModel().selectFirst();

				List<Text> displayCreationName = new ArrayList<>();
				List<String> filesToConcatenate = new ArrayList<>();
				info.setText("");
				for (String namePart : newValue.getNameParts()) { // Associates a name part with a name from the database
					Text namePartText = new Text(namePart + " ");
					namePartText.setStyle("-fx-font-size: 32px;");

					if (Name.getAllNames().containsKey(namePart.toLowerCase())) { // find a recording for the name part
						List<Version> nameVersions = Name.getAllNames().get(namePart.toLowerCase()).getVersions();
						filesToConcatenate.add("file " + NAMES_CORPUS + "/" + nameVersions.get(0).getFileName() + "'");
						nameParts.getItems().add(nameVersions.get(0));
					} else {
						namePartText.setStrikethrough(true); // cross out name part if recording does not exist in database
						info.setText("Some names are not available.");
					}
					displayCreationName.add(namePartText);
				}
				nameParts.getSelectionModel().selectFirst();
				currentCreationName.getChildren().setAll(displayCreationName); // Shows the name of the current creation in the UI

				try { // writes the files to concatenate to a text file
					Files.write(new File("concatenatedFiles.txt").toPath(), filesToConcatenate, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}

				body.setDisable(true);
				String saveInfo = info.getText();
				info.setText("Loading...");
				Concatenate concatenate = new Concatenate();
				concatenate.setOnSucceeded(finished -> {
					info.setText(saveInfo);
					body.setDisable(false);
				});
				new Thread(concatenate).start();
			} else {
				Text chooseName = new Text("Choose Name");
				chooseName.setStyle("-fx-font-size: 32px;");
				currentCreationName.getChildren().setAll(chooseName);
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
				Applet.newAudioClip(new File("normalized.wav").toURI().toURL()).play();
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

		attemptName.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();
			if (creation != null) {
				String creationName = creation.getName();
				body.setDisable(true); // disable UI while recording

				String timestamp = new Timestamp(new Date().getTime()).toString().replace(':','-');
				File filePath = new File(CLASSES + "/" + currentCourse.getText() + "/" + timestamp + "_" + creationName + ".wav");

				RecordAudio recording = new RecordAudio(filePath);
				recording.setOnSucceeded(finished -> { // user can choose to play, save, or delete the recording.
					creation.addAttempt(filePath.getPath());
					pastAttempts.setItems(FXCollections.observableArrayList(creation.getAttempts())); // refresh list
					pastAttempts.getSelectionModel().selectLast();
					body.setDisable(false); // re-enable UI
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
					Creations.getSelectionModel().getSelectedItem().removeAttempt(pastAttempts.getSelectionModel().getSelectedItem());
					pastAttempts.setItems(FXCollections.observableArrayList(Creations.getSelectionModel().getSelectedItem().getAttempts()));
					if (!pastAttempts.getItems().isEmpty()) pastAttempts.getSelectionModel().selectFirst();
				}
			}
		});

		completion.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(numCreations.getText())) {
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

		// Mic level visualizer
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open();

            info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open();

        } catch(LineUnavailableException lue) { lue.printStackTrace(); }

        micThread.start();
    }

    private class Background extends Task<Void> {

        public Background() {

        }

        @Override
        protected Void call() throws Exception {
            targetLine.start();

            byte[] data = new byte[targetLine.getBufferSize() / 5];
            int readBytes;
            while (true) {
                readBytes = targetLine.read(data, 0, data.length);

                double max;
                if (readBytes >=0) {
                    max = (double) (data[0] + (data[1] << 8));
                    for (int p=2;p<readBytes-1;p+=2) {
                        double thisValue = (double) (data[p] + (data[p+1] << 8));
                        if (thisValue>max) max=thisValue;
                    }
                    if (max / 10000 < 0 == false) {
                        soundLevelBar.setProgress(max / 10000);
                    }
                }
            }
        }
    }

    @FXML
	private void showHideButtonAction(ActionEvent event) {
		if (soundLevelBar.isVisible()) {
			soundLevelBar.setVisible(false);
		}
		else {
			soundLevelBar.setVisible(true);
		}
	}
}