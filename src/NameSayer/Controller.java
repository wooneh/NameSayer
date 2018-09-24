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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

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
	@FXML Button goodButton;
	@FXML Button badButton;
	@FXML TextField addNewTextField;
	@FXML TableColumn<Creation, Boolean> Playlist;
	@FXML ComboBox<String> Versions;
	@FXML Text currentCreationName;
	@FXML Text lastRecording;
	@FXML Label wordRating;

	private String selectedName;
	private boolean foundLine;
	private String selectedVersion;
	public void initialize() {

		// Create rating documentation file
		File ratings = new File("Ratings");
		ratings.mkdir();

		// Create text file for all ratings
		File allRatingFile = new File("allRatings");
		if (!allRatingFile.exists()) {
			try (Writer ratingWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("allRatings"), "utf-8"))) {
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
		    if (!data.isEmpty()) {
				selectedName = newValue.getName();
                goodButton.setDisable(false);
                badButton.setDisable(false);
                wordRating.setDisable(false);
            }
            else {
            	goodButton.setDisable(true);
            	badButton.setDisable(true);
			}


			File versionFolder = new File("Ratings/" + selectedName);
		    if (!versionFolder.exists()) {
		    	versionFolder.mkdir();
			}

			if (newValue != null) { // new selection can be null if deleted the last creation
				// Stops the old audio from playing
				String oldFile = Versions.getSelectionModel().getSelectedItem();
				if (oldFile != null) creationPlayers.get(oldValue.getName()).get(oldFile).stop();

				// Shows the name of the current creation in the UI
				currentCreationName.setText(newValue.getName());

				Versions.setItems(FXCollections.observableArrayList(creationPlayers.get(newValue.getName()).keySet()).sorted());
				Versions.getSelectionModel().select(0);
			} else {
				currentCreationName.setText("Choose Creation");
				Versions.setItems(null);
			}
		});

		Versions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			// Create folder for each version clicked and enable rating buttons
			selectedVersion = newValue;


			File versionFolder = new File("Ratings/" + selectedName + "/" + selectedVersion);
			if (!versionFolder.exists()) {
				versionFolder.mkdir();
				selectedVersion = versionFolder.getName();
			}



			// Display rating if text file exists for that creation name
			File folder = new File("./Ratings/" + selectedName);
			File[] listOfFiles = folder.listFiles();
			boolean found = false;
			for (File file : listOfFiles) {
				if (file.isDirectory()) {
					if (file.list().length > 0) {
						found = true;
						File audioFiles = new File("Ratings/" + selectedName + "/" + selectedVersion);
						if (audioFiles.list().length > 0) {
							try {
								file = new File("Ratings/" + selectedName + "/" +
										selectedVersion + "/" + selectedVersion.substring(0, selectedVersion.length() - 4));
								BufferedReader br = new BufferedReader(new FileReader(file));
								wordRating.setText(br.readLine());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					else {
						wordRating.setText("No Rating");
					}
				}
			}
			if (found == false) {
				wordRating.setText("No Rating");
			}

			Creation currentCreation = Creations.getSelectionModel().getSelectedItem();

			// Stops the old audio from playing
			if (currentCreation != null && oldValue != null) {
				AudioClip oldAudio = creationPlayers.get(currentCreation.getName()).get(oldValue);
				if (oldAudio != null) oldAudio.stop();
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
			if (data.isEmpty()) {
				wordRating.setText("");
				goodButton.setDisable(true);
				badButton.setDisable(true);
			}
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

			// Clear allRatings txt file
			File allRatingsFile = new File("allRatings");
			try {
				PrintWriter writer = new PrintWriter(allRatingsFile);
				writer.print("");
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (creation != null) {
				// Gets the currently selected creation to delete
				String creationName = creation.getName();

				Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
				confirmDelete.setHeaderText("Delete Creation");
				confirmDelete.setContentText("Are you sure you want to delete " + creationName + "?");

				confirmDelete.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
					File creationVersionsDirectory = new File("creations/" + creationName);
					File[] creationVersions = creationVersionsDirectory.listFiles();

					// Remove ratings

					File fileToDelete = new File("./Ratings" + "/" + selectedName);
					fileToDelete.delete();

					// remove the folder, row, and associated players
					if (creationVersions != null) for (File version : creationVersions) version.delete();
					if (creationVersionsDirectory.delete()) {
						data.remove(creation);
						File folder = new File("./Ratings");
						File[] listOfFiles = folder.listFiles();
						for (File file : listOfFiles) {
							if (file.isFile()) {
								if (creationName.equals(file.getName())) {
									file.delete();
								}
							}
						}
					}
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

		List<Button> attemptButtons = new ArrayList<>();
		attemptButtons.add(saveAttempt);
		attemptButtons.add(playAttempt);
		attemptButtons.add(trashAttempt);

		attemptName.setOnAction(event -> {
			Creation creation = Creations.getSelectionModel().getSelectedItem();
			if (creation != null) {
				String creationName = creation.getName();
				body.setDisable(true); // disable UI while recording

				// trigger ComboBox event to stop audio from playing
				String currentVersionSelected = Versions.getSelectionModel().getSelectedItem();
				Versions.getSelectionModel().select(null);

				// If the user hasn't chosen to save or delete the last recording, delete the last recording
				if (!attemptButtons.get(0).isDisable()) trashAttempt.fireEvent(new ActionEvent());

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
						for (Button button : attemptButtons) button.setDisable(false);
						lastRecording.setText("Last recording: " + fileName);
						Versions.getSelectionModel().select(currentVersionSelected);

						playAttempt.setOnAction(action -> audioClip.play());

						// disables the attempt buttons and adds the recording to the database
						// if the currently selected creation matches the creation that the recording is for,
						// then add the recording to the ComboBox. Select the recording automatically.
						saveAttempt.setOnAction(action -> {
							lastRecording.setText("Last recording: None");
							audioClip.stop();
							for (Button button : attemptButtons) button.setDisable(true);

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
							new File(filePath).delete();
							for (Button button : attemptButtons) button.setDisable(true);
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
						Versions.getSelectionModel().select(currentVersionSelected);
						audioFile.delete();
						waveForm.delete();
					});

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			});

			new Thread(recording).start();
		});
	}

	@FXML private void badButtonAction() {
		if (selectedVersion == null) {
			return;
		}
		// Write to individual txt file
		wordRating.setText("Bad");
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ratings/" + selectedName + "/" +
				selectedVersion + "/" + selectedVersion.substring(0, selectedVersion.length() - 4)), "utf-8"))) {
			writer.write("Bad");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Write to allRatings txt file
		File file = new File("allRatings");
		try {
			Scanner scanner = new Scanner(file);

			// If no ratings are added, then add the current rating
			if (file.length() == 0) {
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("allRatings"), "utf-8"))) {
					writer.write(selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Bad");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Check if version already exists
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(selectedVersion.substring(0, selectedVersion.length() - 4))) {
					foundLine = true;
					break;
				} else {
					foundLine = false;
				}
			}


			// Replace rating if version already exists
			if (foundLine == true) {
				try {
					List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));

					for (int i = 0; i < fileContent.size(); i++) {
						if (fileContent.get(i).equals(selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Good")) {
							fileContent.set(i, selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Bad");
							break;
						}
					}

					Files.write(file.toPath(), fileContent, StandardCharsets.UTF_8);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (foundLine != true) {
				foundLine = false;
			}

			if (foundLine == false) {
				try {
					String filename = "allRatings";
					FileWriter fw = new FileWriter(filename, true); //the true will append the new data
					fw.write("\n" + selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Bad");//appends the string to the file
					fw.close();
				} catch (IOException ioe) {
					System.err.println("IOException: " + ioe.getMessage());

				}
			}



		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@FXML private void goodButtonAction() {

		if (selectedVersion == null) {
			return;
		}
		wordRating.setText("Good");

		// write to individual txt file
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ratings/" + selectedName + "/" +
				selectedVersion + "/" + selectedVersion.substring(0, selectedVersion.length()-4)), "utf-8"))) {
			writer.write("Good");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Write to allRatings txt file
		File file = new File("allRatings");
		try {
			Scanner scanner = new Scanner(file);

			// If no ratings are entered then add the current rating
			if (file.length() == 0) {
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("allRatings"), "utf-8"))) {
					writer.write(selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Good");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Check if rating already exists
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(selectedVersion.substring(0, selectedVersion.length() - 4))) {
					foundLine = true;
					break;
				} else {
					foundLine = false;
				}
			}


			// Replace rating if version already exists
			if (foundLine == true) {
				try {
					List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));

					for (int i = 0; i < fileContent.size(); i++) {
						if (fileContent.get(i).equals(selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Bad")) {
							fileContent.set(i, selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Good");
							break;
						}
					}

					Files.write(file.toPath(), fileContent, StandardCharsets.UTF_8);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (foundLine != true) {
				foundLine = false;
			}

			if (foundLine == false) {
				try {
					String filename = "allRatings";
					FileWriter fw = new FileWriter(filename, true); //the true will append the new data
					fw.write("\n" + selectedVersion.substring(0, selectedVersion.length() - 4) + "    " + "Good");//appends the string to the file
					fw.close();
				} catch (IOException ioe) {
					System.err.println("IOException: " + ioe.getMessage());

				}
			}



		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}