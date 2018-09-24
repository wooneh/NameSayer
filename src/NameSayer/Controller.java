package NameSayer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
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
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;

public class Controller {
	@FXML VBox body;
	@FXML TableView<Creation> Creations;
	@FXML Button deleteButton;
	@FXML Button playButton;
	@FXML Button nextCreation;
	@FXML Button prevCreation;
	@FXML Button randomCreation;
	@FXML Button addNewButton;
	@FXML Button attemptName;
	@FXML Button testMicButton;
	@FXML Button playAttempt;
	@FXML Button saveAttempt;
	@FXML Button trashAttempt;
	@FXML TextField addNewTextField;
	@FXML TableColumn<Creation, Boolean> Playlist;
	@FXML ComboBox<String> Versions;
	@FXML Text currentCreationName;
	@FXML Text lastRecording;
	@FXML HBox attemptButtons;
	@FXML ToggleGroup rating;
	@FXML RadioButton goodRating;
	@FXML RadioButton badRating;
	@FXML HBox ratingButtons;

	public void initialize() {

		// Create rating documentation file
		File ratings = new File("ratings");
		if (ratings.exists() || ratings.mkdir()) {
			// Create text file for all ratings
			try {
				new File("ratings/allRatings.txt").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}

		TreeSet<Creation> currentPlaylist = new TreeSet<>();
		Random random = new Random();
		Map<String, Map<String,AudioClip>> creationPlayers = new HashMap<>();
		ObservableList<Creation> data = Creations.getItems();
		Creations.setItems(new SortedList<>(data,Comparator.naturalOrder()));
		Creations.setEditable(true);

		// Add an event listener the TableView that triggers when the selected item changes
		// The selected item overrides the current playlist selection. If the item that is
		// selected is part of the playlist, the next/prev buttons will go to the next checked item
		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // new selection can be null if deleted the last creation
				File creationRatingFolder = new File("ratings/" + newValue.getName());
				if (!creationRatingFolder.exists()) creationRatingFolder.mkdir();

				// Stops the old audio from playing
				String oldFile = Versions.getSelectionModel().getSelectedItem();
				if (oldFile != null) creationPlayers.get(oldValue.getName()).get(oldFile).stop();

				// Shows the name of the current creation in the UI
				currentCreationName.setText(newValue.getName());

				// Update the versions dropdown with the associated audio files of the current creation
				Versions.setItems(FXCollections.observableArrayList(creationPlayers.get(newValue.getName()).keySet()).sorted());
				Versions.getSelectionModel().selectFirst();
			} else {
				currentCreationName.setText("Choose Creation");
				Versions.setItems(null);
			}

			// Versions are empty upon creation, or if there are no creations
			if (Versions.getItems() == null || Versions.getItems().isEmpty()) {
				ratingButtons.setDisable(true);
				if (rating.getSelectedToggle() != null) rating.getSelectedToggle().setSelected(false);
			} else ratingButtons.setDisable(false);
		});

		Versions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Creation currentCreation = Creations.getSelectionModel().getSelectedItem();

			// If user added a recording to an empty creation, enable the rating buttons
			if (newValue != null) ratingButtons.setDisable(false);

			if (currentCreation != null) {
				String creationName = currentCreation.getName();

				// Stops the old audio from playing
				AudioClip oldAudio = creationPlayers.get(currentCreation.getName()).get(oldValue);
				if (oldAudio != null) oldAudio.stop();

				File folder = new File("ratings/" + creationName);
				File[] versionRatings = folder.listFiles();

				// Display rating if text file exists for that creation name
				if (rating.getSelectedToggle() != null) rating.getSelectedToggle().setSelected(false);
				if (versionRatings != null) {
					for (File ratingFile : versionRatings) {
						if (ratingFile.getName().substring(0, ratingFile.getName().length() - 4).equals(newValue)) {
							try (BufferedReader reader = new BufferedReader(new FileReader(ratingFile))) {
								String versionRating = reader.readLine();
								if (versionRating != null && versionRating.equals("Good")) rating.selectToggle(goodRating);
								else rating.selectToggle(badRating);

								reader.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		// The callback function is called whenever the checked state
		// of the creation changes. The current playlist is then updated accordingly.
		Playlist.setCellValueFactory(creationData -> {
			Creation creation = creationData.getValue();

			if (creation.isChecked()) currentPlaylist.add(creation);
			else currentPlaylist.remove(creation);

			// when starting a playlist, automatically select the first creation in the queue.
			if (currentPlaylist.size() == 1) Creations.getSelectionModel().select(currentPlaylist.first());

			return creation.getChecked();
		});

		// Adds a checkbox to correspond to the boolean value in the column
		Playlist.setCellFactory(CheckBoxTableCell.forTableColumn(Playlist));

		// Add an event listener to the list of creations. When it changes, ie when a creation is added
		// or removed, perform the corresponding action to the dictionary of creations.
		data.addListener((ListChangeListener.Change<? extends Creation> observable) -> {
			while (observable.next()) {
				observable.getRemoved().forEach(creation ->{
					creationPlayers.remove(creation.getName());
					currentPlaylist.remove(creation);
				});

				observable.getAddedSubList().forEach(creation -> {
					String creationName = creation.getName();
					Map<String, AudioClip> creationVideos = new HashMap<>();

					// pass in a File object URI string as the AudioClip object does not accept relative paths
					File[] creationVideoFiles = new File("creations/" + creationName).listFiles();

					if (creationVideoFiles != null) {
						for (File creationVideoFile : creationVideoFiles) {
							try {
								creationVideos.put(creationVideoFile.getName(), Applet.newAudioClip(creationVideoFile.toURI().toURL()));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
					}

					creationPlayers.put(creationName, creationVideos);
					Creations.getSelectionModel().select(creation); // select the newly added creation
				});
			}
		});

		File creationPath = new File("creations");
		if (creationPath.exists() || creationPath.mkdir()) { // Create the creation directory  if one doesn't already exist
			File[] creations = creationPath.listFiles();
			if (creations != null) { // Populate table with existing Creations and load media files
				for (File creationFolder : creations) {
					String creationName = creationFolder.getName();
					data.add(new Creation(creationName));
				}

				Creations.getSelectionModel().clearSelection();
			}
		}

		// Add an event listener to the Play button such that when clicked, plays the current video.
		playButton.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			String currentVersion = Versions.getSelectionModel().getSelectedItem();

			if (currentSelection != null && currentVersion != null) creationPlayers.get(currentSelection.getName()).get(currentVersion).play();
		});

		// Loads the next queued creation. Cycles to the beginning at the end.
		nextCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			if (currentSelection != null) {
				Creation nextPlaying = currentPlaylist.higher(currentSelection);

				if (nextPlaying != null) Creations.getSelectionModel().select(nextPlaying);
				else if (currentPlaylist.size() > 0) Creations.getSelectionModel().select(currentPlaylist.first());
			}
		});

		// Loads the previously queued creation. Cycles to the end at at the beginning.
		prevCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			if (currentSelection != null) {
				Creation nextPlaying = currentPlaylist.lower(currentSelection);

				if (nextPlaying != null) Creations.getSelectionModel().select(nextPlaying);
				else if (currentPlaylist.size() > 0) Creations.getSelectionModel().select(currentPlaylist.last());
			}
		});
		// select a random creation from the playlist
		randomCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			Creation nextPlaying = currentSelection;
			if (currentSelection != null) {
				if (currentPlaylist.size() > 1) {
					Iterator<Creation> iteratePlaylist;
					int randomIndex;

					// ensures that the next creation is not the same one that is currently selected.
					// if it is, select a different random creation from the playlist
					while (nextPlaying.equals(currentSelection)) {
						randomIndex = random.nextInt(currentPlaylist.size());
						iteratePlaylist = currentPlaylist.iterator();
						for (int i = 0; i <= randomIndex; i++) nextPlaying = iteratePlaylist.next();
					}

				} else if (currentPlaylist.size() == 1) nextPlaying = currentPlaylist.first();
				Creations.getSelectionModel().select(nextPlaying);
			}
		});

		// Add an event listener to the Delete button such that when clicked, prompts deletion of creation.
		deleteButton.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();

			if (creation != null) {
				// Gets the currently selected creation to delete
				String creationName = creation.getName();

				Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
				confirmDelete.setHeaderText("Delete Creation");
				confirmDelete.setContentText("Are you sure you want to delete " + creationName + "?");

				confirmDelete.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
					File creationVersionsDirectory = new File("creations/" + creationName);
					File[] creationVersions = creationVersionsDirectory.listFiles();

					File creationRatingDirectory = new File("ratings/" + creationName);
					File[] creationRatings = creationRatingDirectory.listFiles();

					// remove the creation folder & Ratings, row, and associated players
					if (creationVersions != null) for (File version : creationVersions) version.delete();
					if (creationRatings != null) for (File rating : creationRatings) rating.delete();
					if (creationVersionsDirectory.delete() && creationRatingDirectory.delete()) data.remove(creation);
				});
			}
		});

		addNewButton.setOnAction(event -> addNewTextField.fireEvent(new ActionEvent()));

		addNewTextField.setOnAction(event -> {
			Creation newCreation = new Creation(addNewTextField.getCharacters().toString().trim());
			String creationName = newCreation.getName();

			// check if creation contains invalid characters (not letters, numbers, underscores, or hyphens)
			if (creationName.matches("[a-zA-Z0-9 _-]*") && !creationName.isEmpty()) {
				// Check if the creation name already exists. If it does, then the user cannot create one with the same name
				// so they must either choose a different name, otherwise the existing creation is automatically selected.
				if (data.contains(newCreation)) {
					Alert confirmOverwrite = new Alert(Alert.AlertType.INFORMATION);
					confirmOverwrite.setHeaderText("There is already an existing creation called " + creationName);
					confirmOverwrite.setContentText("Choose a different name, or add your own recording to the creation.");
					confirmOverwrite.showAndWait();
					Creations.getSelectionModel().select(newCreation);
				} else if (new File("creations/" + creationName).mkdir()) data.add(newCreation);
			} else {
				Alert invalidCharacters = new Alert(Alert.AlertType.WARNING);
				invalidCharacters.setHeaderText("Your creation contains invalid characters");
				invalidCharacters.setContentText("Please choose another name for your creation.");
				invalidCharacters.showAndWait();
			}

			addNewTextField.clear();
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
							if (!data.contains(creation)) data.add(creation);
							creationPlayers.get(creationName).put(fileName, audioClip);

							Creation currentCreation = Creations.getSelectionModel().getSelectedItem();
							if (creation.equals(currentCreation)) {
								// refresh the drop down list
								Versions.setItems(FXCollections.observableArrayList(creationPlayers.get(creation.getName()).keySet()).sorted());
								Versions.getSelectionModel().select(fileName);
							}
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
						if (audioFile.delete() && waveForm.delete())Versions.getSelectionModel().select(currentVersionSelected);
					});

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			});

			new Thread(recording).start();
		});

		rating.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			String creationName = Creations.getSelectionModel().getSelectedItem().getName();
			String versionName = Versions.getSelectionModel().getSelectedItem();

			if (newValue != null && versionName != null) {
				String ratingFileName = "ratings/" + creationName + "/" + versionName + ".txt";
				File ratingFile = new File(ratingFileName);
				String newRating = newValue.getUserData().toString();

				try {
					if (ratingFile.exists() || ratingFile.createNewFile()) {
						BufferedReader reader = new BufferedReader(new FileReader(ratingFile));
						String existingRating = reader.readLine();
						if (existingRating == null) existingRating = "";

						// Only update files if the rating has changed
						if (!existingRating.equals(newRating)) {
							// Update individual rating file
							BufferedWriter individualFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ratingFile), "utf-8"));
							individualFileWriter.write(newValue.getUserData().toString());
							individualFileWriter.close();

							String allRatings = "ratings/allRatings.txt";

							// Append to master file if this is first rating
							if (existingRating.isEmpty()) {
								FileWriter masterFileWriter = new FileWriter(allRatings, true);
								masterFileWriter.write(versionName + '\t' + newRating + System.lineSeparator());
								masterFileWriter.close();
							} else { // change existing line since this isn't the first rating
								Path allRatingsPath = new File(allRatings).toPath();
								List<String> ratingsContent = new ArrayList<>(Files.readAllLines(allRatingsPath, StandardCharsets.UTF_8));

								for (int i = 0; i < ratingsContent.size(); i++) {
									if (ratingsContent.get(i).contains(versionName)) {
										ratingsContent.set(i, versionName + '\t' + newRating);
										break;
									}
								}

								Files.write(allRatingsPath, ratingsContent, StandardCharsets.UTF_8);
							}
						}

						reader.close();
					}
				} catch (IOException e) {
				 	e.printStackTrace();
				}
			}
		});
	}
}