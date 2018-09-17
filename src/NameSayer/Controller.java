package NameSayer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.media.AudioClip;
import javafx.concurrent.Task;
import javafx.scene.text.Text;

import java.io.*;

import java.util.*;

public class Controller {
	@FXML private TableView<Creation> Creations;
	@FXML private Button deleteButton;
	@FXML private Button playButton;
	@FXML private Button nextCreation;
	@FXML private Button prevCreation;
	@FXML private Button addNewButton;
	@FXML private TextField addNewTextField;
	@FXML private Button randomCreation;
	@FXML private TableColumn<Creation, Boolean> Playlist;
	@FXML private ComboBox<String> Versions;
	@FXML private Text currentCreationName;

	private ObservableList<Creation> data;
	private Map<String, Map<String,AudioClip>> creationPlayers = new HashMap<>();

	public void initialize() {
		TreeSet<Creation> currentPlaylist = new TreeSet<>();
		Random random = new Random();

		data = Creations.getItems();
		Creations.setEditable(true);

		// Add an event listener the TableView that triggers when the selected item changes
		// The selected item overrides the current playlist selection. If the item that is
		// selected is part of the playlist, the next/prev buttons will go to the next checked item
		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // new selection can be null if deleted the last creation
				// Stops the old audio from playing
				String oldFile = Versions.getSelectionModel().getSelectedItem();
				if (oldFile != null) creationPlayers.get(oldValue.getName()).get(oldFile).stop();

				// Shows the name of the current creation in the UI
				currentCreationName.setText(newValue.getName());

				Versions.setItems(FXCollections.observableArrayList(creationPlayers.get(newValue.getName()).keySet()));
				Versions.getSelectionModel().select(0);
			}
		});

		Versions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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

			if (creation.isChecked()) {
				currentPlaylist.add(creation);
			} else {
				currentPlaylist.remove(creation);
			}

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
				observable.getRemoved().forEach(creation -> creationPlayers.remove(creation.getName()));

				observable.getAddedSubList().forEach(creation -> {
					String creationName = creation.getName();
					Map<String, AudioClip> creationVideos = new HashMap<>();

					// pass in a File object URI string as the Media object does not accept relative paths
					File[] creationVideoFiles = new File(getCreationDirectory(creation)).listFiles();

					if (creationVideoFiles != null) {
						for (File creationVideoFile : creationVideoFiles) {
							creationVideos.put(creationVideoFile.getName(), new AudioClip(creationVideoFile.toURI().toString()));
						}
					}

					creationPlayers.put(creationName, creationVideos);
				});
			}
		});

		File creationPath = new File("creations");
		File[] creations = creationPath.listFiles();

		// Create the creation directory  if one doesn't already exist
		if (!creationPath.exists()) creationPath.mkdir();

		// Populate table with existing Creations and load media files
		if (creations != null) {
			for (File creationFolder : creations) {
				String creationName = creationFolder.getName();
				data.add(new Creation(creationName));
			}
		}

		// Add an event listener to the Play button such that when clicked, plays the current video.
		playButton.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			String currentVersion = Versions.getSelectionModel().getSelectedItem();

			if (currentSelection != null) creationPlayers.get(currentSelection.getName()).get(currentVersion).play();
		});

		// Loads the next queued creation. Cycles to the beginning at the end.
		nextCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			if (currentSelection != null) {
				Creation nextPlaying = currentPlaylist.higher(currentSelection);

				if (nextPlaying != null) {
					Creations.getSelectionModel().select(nextPlaying);
				} else if (currentPlaylist.size() > 0){
					Creations.getSelectionModel().select(currentPlaylist.first());
				}
			}
		});

		// Loads the previously queued creation. Cycles to the end at at the beginning.
		prevCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			if (currentSelection != null) {
				Creation nextPlaying = currentPlaylist.lower(currentSelection);

				if (nextPlaying != null) {
					Creations.getSelectionModel().select(nextPlaying);
				} else if (currentPlaylist.size() > 0) {
					Creations.getSelectionModel().select(currentPlaylist.last());
				}
			}
		});

		// select a random creation from the database and choose the next checked creation
		randomCreation.setOnAction(event -> {
			Creation currentSelection = Creations.getSelectionModel().getSelectedItem();
			if (currentSelection != null) {
				Creation nextPlaying = currentPlaylist.higher(data.get(random.nextInt(data.size())));

				// ensures that the next creation is not the same one that is currently selected.
				// if it is, then select the first checked creation after the one that is currently selected.
				if (nextPlaying != null) {
					if (nextPlaying.equals(currentSelection)) nextPlaying = currentPlaylist.higher(nextPlaying);
				} else if (currentPlaylist.size() > 0){
					if (currentSelection.equals(currentPlaylist.first())) nextPlaying = currentPlaylist.higher(currentPlaylist.first());
				}

				if (nextPlaying != null) {
					Creations.getSelectionModel().select(nextPlaying);
				} else if (currentPlaylist.size() > 0){
					Creations.getSelectionModel().select(currentPlaylist.first());
				}
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
					File creationVersionsDirectory = new File(getCreationDirectory(creation));
					File[] creationVersions = creationVersionsDirectory.listFiles();

					if (creationVersions != null) {
						for (File version : creationVersions) {
							version.delete();
						}
					}

					// remove the folder, row, and associated MediaPlayer
					creationVersionsDirectory.delete();
					data.remove(creation);
				});
			}
		});

		addNewButton.setOnAction(event -> addNewTextField.fireEvent(new ActionEvent()));

		addNewTextField.setOnAction(event -> {
			Creation newCreation = new Creation(addNewTextField.getCharacters().toString().trim());
			String creationName = newCreation.getName();

			// check if creation contains invalid characters (not letters, numbers, underscores, or hyphens)
			if (creationName.matches("[a-zA-Z0-9 _-]*") && !creationName.isEmpty()) {

				// check if creation already exists, and ask whether we should overwrite it. This ovewrites the existing creation
				// from the table and its corresponding MediaPlayer, but not the folder containing the creation.
				if (data.contains(newCreation)) {
					Alert confirmOverwrite = new Alert(Alert.AlertType.CONFIRMATION);
					confirmOverwrite.setHeaderText("There is already an existing creation called " + creationName);
					confirmOverwrite.setContentText("Would you like to overwrite the existing creation?");
					confirmOverwrite.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
						data.remove(newCreation);
						addCreation(newCreation);
					});
				} else {
					File newCreationDirectory = new File(getCreationDirectory(newCreation));
					newCreationDirectory.mkdir();

					addCreation(newCreation);
				}
			} else {
				Alert invalidCharacters = new Alert(Alert.AlertType.WARNING);
				invalidCharacters.setHeaderText("Your creation contains invalid characters");
				invalidCharacters.setContentText("Please choose another name for your creation.");
				invalidCharacters.showAndWait();
			}

		});
	}

	/**
	 * This method uses ffmpeg to create a video and record audio input to add a creation.
	 * A creation may be redone if the user is not satisfied with the audio.
	 */
	private void addCreation(Creation creation) {
		String creationName = creation.getName();
		String creationDirectory = getCreationDirectory(creation);

		ProcessBuilder createVideo = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -loglevel quiet -f lavfi -y -i color=c=black:s=320x240:d=5 -vf drawtext=\"fontsize=16:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + creationName + "'\" \"" + creationDirectory + creationName + "Temp.mp4\"");
		ProcessBuilder createAudio = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -loglevel quiet -f alsa -y -ac 1 -ar 44100 -i default -t 5 -strict -2 \"" + creationDirectory + creationName + ".m4a\"");
		ProcessBuilder merge = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -loglevel quiet -y -i \"" + creationDirectory + creationName + "Temp.mp4\" -i \"" + creationDirectory + creationName + ".m4a\" -c copy \"" + creationDirectory + creationName + ".mp4\"");

		Alert voicePrompt = new Alert(Alert.AlertType.INFORMATION);
		voicePrompt.setHeaderText("Your voice will now be recorded.");
		voicePrompt.setContentText("Press ENTER or hit OK when you are ready.");
		voicePrompt.showAndWait();

		Alert voiceRecording = new Alert(Alert.AlertType.INFORMATION);
		voiceRecording.setHeaderText("Your voice is being recorded.");
		voiceRecording.setContentText("Please speak clearly into the microphone.");
		voiceRecording.show();

		Task<Void> recordAudio = new Task<Void>() {
			@Override
			protected Void call() throws IOException, InterruptedException {
				Process createVideoProcess = createVideo.start();
				createVideoProcess.waitFor();
				Process createAudioProcess = createAudio.start();
				createAudioProcess.waitFor();
				Process mergeProcess = merge.start();
				mergeProcess.waitFor();
				return null;
			}
		};

		// Adds an event listener when the audio finishes recording.
		recordAudio.setOnSucceeded(event -> {
			voiceRecording.close();
			data.add(creation);

			File rawVideo = new File(creationDirectory + creationName + "Temp.mp4");
			File rawAudio = new File(creationDirectory + creationName + ".m4a");
			rawVideo.delete();
			rawAudio.delete();

			Alert playRecording = new Alert(Alert.AlertType.CONFIRMATION);
			playRecording.setHeaderText("Your creation has been saved.");
			playRecording.setContentText("Would you like to hear the recorded audio?");
			playRecording.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
				Creations.getSelectionModel().select(creation);
				creationPlayers.get(creationName).get(Versions.getSelectionModel().getSelectedItem()).play();

				Alert keepAudio = new Alert(Alert.AlertType.CONFIRMATION);
				keepAudio.setHeaderText("Your recording is now playing.");
				keepAudio.setContentText("Would you like to keep or redo the recording?");

				ButtonType redo = new ButtonType("Redo");
				ButtonType keep = new ButtonType("Keep");

				keepAudio.getButtonTypes().setAll(keep, redo);

				keepAudio.showAndWait().filter(audioResponse -> audioResponse == redo).ifPresent(audioResponse -> {
					data.remove(creation);
					addCreation(creation);
				});
			});
		});

		Thread thread = new Thread(recordAudio);
		thread.start();
	}

	private String getCreationDirectory(Creation creation) {
		String creationName = creation.getName();
		return "creations/" + creationName + "/";
	}

}
