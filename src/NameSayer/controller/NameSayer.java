package NameSayer.controller;

import NameSayer.Creation;
import NameSayer.Version;
import NameSayer.task.Concatenate;
import NameSayer.task.GenerateWaveForm;
import NameSayer.task.RecordAudio;
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

public class NameSayer {
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
	@FXML ComboBox<Version> Versions;
	@FXML Text currentCreationName;
	@FXML Text lastRecording;
	@FXML HBox attemptButtons;
	@FXML ToggleGroup rating;
	@FXML RadioButton goodRating;
	@FXML RadioButton badRating;
	@FXML HBox ratingButtons;
	@FXML Text currentCourse;

	/**
	 * This method adds valid names to the practice list.
	 * @param practiceNames The list of names to practice
	 */
	public void setPracticeNames(String[] practiceNames) {
		for (String name : practiceNames) { // make sure the name is not empty or contains invalid characters
			name = name.trim(); // remove whitespace
			Creation newCreation = new Creation(name.trim());
			if (!Creations.getItems().contains(newCreation) && !name.isEmpty() && name.matches("[a-zA-Z0-9 _-]*")) Creations.getItems().add(newCreation);
		}
	}

	/**
	 * This method creates a folder (if one doesn't exist) for the inputted class
	 * @param courseCode Course code for the selected class
	 */
	public void setCourseCode(String courseCode) {
		File classFolder = new File(courseCode);
		if (classFolder.exists() || classFolder.mkdir()) currentCourse.setText(courseCode);
	}

	public void initialize() {
		Map<String, Map<String,AudioClip>> names = new HashMap<>();
		Map<String, String> ratings = new HashMap<>();
		File ratingFile = new File("ratings.txt");
		File namePath = new File("names");

		if (namePath.exists() || namePath.mkdir()) { // Create the name directory  if one doesn't already exist
			File[] nameAudioFiles = namePath.listFiles();
			if (nameAudioFiles != null) { // Populate table with existing names and load media files
				for (File nameAudioFile : nameAudioFiles) {
					String[] splitFile = nameAudioFile.getName().split("_");
					String name = splitFile[splitFile.length - 1].toLowerCase();
					String newName = name.substring(0, name.length() - 4);

					if (names.containsKey(newName)) { // if there is already a name in the database, add the recording to the name
						try { // add the recording to the existing creation
							names.get(newName).put(nameAudioFile.getName(), Applet.newAudioClip(nameAudioFile.toURI().toURL()));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					} else { // otherwise create a new entry in the database and add the name and recording
						Map<String, AudioClip> creationVideos = new HashMap<>();

						try {
							creationVideos.put(nameAudioFile.getName(), Applet.newAudioClip(nameAudioFile.toURI().toURL()));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

						names.put(newName, creationVideos);
					}
				}
			}
		}

		try { // loads the ratings for each recording
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

		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // new selection can be null if deleted the last creation
				currentCreationName.setText(newValue.getName()); // Shows the name of the current creation in the UI
				Versions.getItems().clear();

				List<String> filesToConcatenate = new ArrayList<>(); // Associates a name part with a name from the database
				for (String namePart : newValue.getNameParts()) {
					Map<String, AudioClip> clips = names.get(namePart.toLowerCase());
					if (clips != null) {
						String[] files = clips.keySet().toArray(new String[clips.size()]);
						filesToConcatenate.add("file 'names/" + files[0] + "'");
						Versions.getItems().add(new Version(files[0]));
					}
				}
				Versions.getSelectionModel().selectFirst();

				try { // writes the files to concatenate to a text file for ffmpeg to process
					Files.write(new File("concatenatedFiles.txt").toPath(), filesToConcatenate, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}

				new Thread(new Concatenate()).start();
			}
		});

		Versions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println(newValue);
			if (newValue != null) {
				String versionRating = ratings.get(newValue.getFileName()); // display rating
				if (rating.getSelectedToggle() != null) rating.getSelectedToggle().setSelected(false);
				if (versionRating != null && versionRating.equals("Good")) rating.selectToggle(goodRating);
				else if (versionRating != null && versionRating.equals("Bad")) rating.selectToggle(badRating);
			}
		});

		playButton.setOnAction(event -> { // plays the selected audio.
			try {
				Applet.newAudioClip(new File("concatenated.wav").toURI().toURL()).play();
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
				if (!attemptButtons.isDisable()) trashAttempt.fireEvent(new ActionEvent()); // delete the last recording
				body.setDisable(true); // disable UI while recording

				String timestamp = new Timestamp(new Date().getTime()).toString().replace(':','-');
				String filePath = currentCourse.getText() + "/" + timestamp + "_" + creationName + ".wav";

				RecordAudio recording = new RecordAudio(filePath);
				lastRecording.setText("Recording voice...");

				recording.setOnSucceeded(finished -> { // user can choose to play, save, or delete the recording.
					try {
						AudioClip audioClip = Applet.newAudioClip(new File(filePath).toURI().toURL());
						body.setDisable(false);
						attemptButtons.setDisable(false);
						lastRecording.setText("Last recording: " + creationName);

						playAttempt.setOnAction(action -> audioClip.play());

						saveAttempt.setOnAction(action -> { // disables the attempt buttons and saves recording
							lastRecording.setText("Last recording: None");
							audioClip.stop();
							attemptButtons.setDisable(true);
						});

						trashAttempt.setOnAction(action -> { // disables the attempt buttons and deletes recording
							lastRecording.setText("Last recording: None");
							audioClip.stop();
							if (new File(filePath).delete()) attemptButtons.setDisable(true);
						});

					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				});

				new Thread(recording).start(); // starts recording the user's voice for 5 seconds.
			}
		});

		testMicButton.setOnAction(event -> {
			File audioFile = new File("recordOut.wav");
			RecordAudio recording = new RecordAudio(audioFile.getName());

			String lastRecordingText = lastRecording.getText(); // save the current text
			lastRecording.setText("Recording voice...");

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
					});

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			});

			new Thread(recording).start();
		});

		rating.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			String versionName = Versions.getSelectionModel().getSelectedItem().getFileName();

			if (newValue != null) {
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