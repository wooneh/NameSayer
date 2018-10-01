package NameSayer.controller;

import NameSayer.Creation;
import NameSayer.task.GenerateWaveForm;
import NameSayer.task.RecordAudio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;

public class NameSayerController {
	@FXML VBox body;
	@FXML TableView<Creation> Creations;
	@FXML Button playButton;
	@FXML Button nextCreation;
	@FXML Button prevCreation;
	@FXML Button attemptName;
	@FXML Button testMicButton;
	@FXML Button playAttempt;
	@FXML Button saveAttempt;
	@FXML Button trashAttempt;
	@FXML ComboBox<String> Versions;
	@FXML Text currentCreationName;
	@FXML Text lastRecording;
	@FXML HBox attemptButtons;
	@FXML ToggleGroup rating;
	@FXML RadioButton goodRating;
	@FXML RadioButton badRating;
	@FXML HBox ratingButtons;

	public void setPracticeNames(String[] practiceNames) {
		ObservableList<Creation> creations = Creations.getItems();
		for (String i : practiceNames) creations.add(new Creation(i));
	}

	public void initialize() {
		Map<String, Map<String,AudioClip>> names = new HashMap<>();
		Map<String, String> ratings = new HashMap<>();
		File ratingFile = new File("ratings.txt");
		File creationPath = new File("names");

		// TODO: refactor creations -> names for the recordings in the database
		if (creationPath.exists() || creationPath.mkdir()) { // Create the creation directory  if one doesn't already exist
			File[] creations = creationPath.listFiles();
			if (creations != null) { // Populate table with existing Creations and load media files
				for (File creationVideoFile : creations) {
					String[] fileName = creationVideoFile.getName().split("_");
					String name = fileName[fileName.length - 1].toLowerCase();
					String newName = name.substring(0, name.length() - 4);

					if (names.containsKey(newName)) {
						try { // add the recording to the existing creation
							names.get(newName).put(creationVideoFile.getName(), Applet.newAudioClip(creationVideoFile.toURI().toURL()));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					} else {
						Creations.getItems().add(new Creation(newName));
						Map<String, AudioClip> creationVideos = new HashMap<>();

						try {
							creationVideos.put(creationVideoFile.getName(), Applet.newAudioClip(creationVideoFile.toURI().toURL()));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

						names.put(newName, creationVideos);
					}
				}
			}
		}

		// loads the ratings for each recording
		try {
			if (ratingFile.exists() || ratingFile.createNewFile()) {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(ratingFile.toPath(), StandardCharsets.UTF_8));

				for (String content : fileContent) {
					String[] splitRating = content.split("\t"); // splits the line into FILENAME, RATING
					ratings.put(splitRating[0], splitRating[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add an event listener the TableView that triggers when the selected item changes
		// The selected item overrides the current playlist selection. If the item that is
		// selected is part of the playlist, the next/prev buttons will go to the next checked item
		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // new selection can be null if deleted the last creation
				// Stops the old audio from playing
				String oldFile = Versions.getSelectionModel().getSelectedItem();
				if (oldFile != null) names.get(oldValue.getName()).get(oldFile).stop();

				// Shows the name of the current creation in the UI
				currentCreationName.setText(newValue.getName());

				// Update the versions dropdown with the associated audio files of the current creation
				Versions.setItems(FXCollections.observableArrayList(names.get(newValue.getName()).keySet()).sorted());
				Versions.getSelectionModel().selectFirst();

				// Display rating if text file exists for that creation name
				String versionRating = ratings.get(Versions.getSelectionModel().getSelectedItem());
				if (rating.getSelectedToggle() != null) rating.getSelectedToggle().setSelected(false);
				if (versionRating != null && versionRating.equals("Good")) rating.selectToggle(goodRating);
				else if (versionRating != null && versionRating.equals("Bad")) rating.selectToggle(badRating);
			}
		});

		// Add an event listener to the Play button such that when clicked, plays the current video.
		playButton.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			String currentVersion = Versions.getSelectionModel().getSelectedItem();

			if (currentSelection != null && currentVersion != null) names.get(currentSelection.getName()).get(currentVersion).play();
		});

		// Loads the next queued creation. Cycles to the beginning at the end.
		nextCreation.setOnAction(event -> {
			if (Creations.getSelectionModel().isSelected(Creations.getItems().size() - 1)) Creations.getSelectionModel().selectFirst();
			else Creations.getSelectionModel().selectNext();

			Creations.scrollTo(Creations.getSelectionModel().getSelectedItem());
		});

		// Loads the previously queued creation. Cycles to the end at at the beginning.
		prevCreation.setOnAction(event -> {
			if (Creations.getSelectionModel().isSelected(0)) Creations.getSelectionModel().selectLast();
			else Creations.getSelectionModel().selectPrevious();

			Creations.scrollTo(Creations.getSelectionModel().getSelectedItem());
		});

		attemptName.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();
			if (creation != null) {
				String creationName = creation.getName();
				body.setDisable(true); // disable UI while recording

				// trigger ComboBox event to stop audio from playing
				String currentVersionSelected = Versions.getSelectionModel().getSelectedItem();
				Versions.getSelectionModel().select(null);

				// If the user hasn't chosen to save or delete the last recording, delete the last recording
				if (!attemptButtons.isDisable()) trashAttempt.fireEvent(new ActionEvent());

				// filename can't have colon...
				String timestamp = new Timestamp(new Date().getTime()).toString().replace(':','-');
				String fileName = timestamp + "_" + creationName + ".wav";
				String filePath = "creations/" + creationName + "/" + fileName;

				RecordAudio recording = new RecordAudio(filePath);
				lastRecording.setText("Recording voice...");

				// Starts recording the user's voice for 5 seconds.
				// When the recording is finished, attempt buttons are enabled
				// and the user can choose to play, save, or delete the recording.
				recording.setOnSucceeded(finished -> {
					try {
						AudioClip audioClip = Applet.newAudioClip(new File(filePath).toURI().toURL());
						body.setDisable(false);
						attemptButtons.setDisable(false);
						lastRecording.setText("Last recording: " + fileName);
						Versions.getSelectionModel().select(currentVersionSelected);

						playAttempt.setOnAction(action -> audioClip.play());

						// disables the attempt buttons and adds the recording to the database
						// if the currently selected creation matches the creation that the recording is for,
						// then add the recording to the ComboBox. Select the recording automatically.
						saveAttempt.setOnAction(action -> {
							lastRecording.setText("Last recording: None");
							audioClip.stop();
							attemptButtons.setDisable(true);

							// If the user deleted the name before saving the recording, then re-add the creation
							names.get(creationName).put(fileName, audioClip);
						});

						trashAttempt.setOnAction(action -> {
							lastRecording.setText("Last recording: None");
							audioClip.stop();
							if (new File(filePath).delete()) attemptButtons.setDisable(true);
						});

					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				});

				new Thread(recording).start();
			}
		});

		testMicButton.setOnAction(event -> {
			File audioFile = new File("recordOut.wav");
			RecordAudio recording = new RecordAudio(audioFile.getName());

			String lastRecordingText = lastRecording.getText(); // save the current text
			lastRecording.setText("Recording voice...");

			// trigger ComboBox event to stop audio from playing
			String currentVersionSelected = Versions.getSelectionModel().getSelectedItem();
			Versions.getSelectionModel().select(null);

			body.setDisable(true); // disable UI while recording

			// when recording is finished, generate audio waveform and play back the audio in a dialog
			recording.setOnSucceeded(finished -> {
				try {
					GenerateWaveForm waveFormProcess = new GenerateWaveForm(audioFile.getName());
					Applet.newAudioClip(audioFile.toURI().toURL()).play();
					lastRecording.setText(lastRecordingText);

					new Thread(waveFormProcess).start();
					waveFormProcess.setOnSucceeded(generated -> {
						File waveForm = new File("waveform.png");

						Alert playTestVoice = new Alert(Alert.AlertType.INFORMATION);
						playTestVoice.setHeaderText("Audio Waveform:");
						playTestVoice.setContentText("If you don't hear anything, please check your microphone settings.");

						try {
							playTestVoice.setGraphic(new ImageView(new Image(waveForm.toURI().toURL().toString())));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

						playTestVoice.showAndWait();

						body.setDisable(false);
						if (audioFile.delete() && waveForm.delete()) Versions.getSelectionModel().select(currentVersionSelected);
					});

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			});

			new Thread(recording).start();
		});

		rating.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			String versionName = Versions.getSelectionModel().getSelectedItem();

			if (newValue != null && versionName != null) {
				String newRating = newValue.getUserData().toString();

				if (!newRating.equals(ratings.get(versionName))) { // only write to file if the existing rating has changed
					try {
						if (ratings.containsKey(versionName)) { // if a rating already exists, change the rating
							List<String> ratingsContent = new ArrayList<>(Files.readAllLines(ratingFile.toPath(), StandardCharsets.UTF_8));

							for (int i = 0; i < ratingsContent.size(); i++) {
								if (ratingsContent.get(i).contains(versionName)) {
									ratingsContent.set(i, versionName + '\t' + newRating);
									break;
								}
							}

							Files.write(ratingFile.toPath(), ratingsContent, StandardCharsets.UTF_8);
						} else { // otherwise append a new line to the  file
							FileWriter masterFileWriter = new FileWriter(ratingFile, true);
							masterFileWriter.write(versionName + '\t' + newRating + System.lineSeparator());
							masterFileWriter.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					ratings.put(versionName, newValue.getUserData().toString());
				}
			}
		});
	}
}