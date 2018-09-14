package NameSayer;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.concurrent.Task;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
	@FXML
	private TableView<Creation> Creations;
	@FXML
	private MediaView Video;
	@FXML
	private Button deleteButton;
	@FXML
	private Button playButton;
	@FXML
	private Button nextCreation;
	@FXML
	private Button prevCreation;
	@FXML
	private Button addNewButton;
	@FXML
	private TextField addNewTextField;
	@FXML
	private TableColumn<Creation, Boolean> Playlist;

	private ObservableList<Creation> data;
	private Map<String, List<MediaPlayer>> creationPlayers;
	private List<Creation> currentPlaylist;
	private int nowPlaying;

	public void initialize() {
		data = Creations.getItems();

		creationPlayers = new HashMap<>();
		currentPlaylist = new ArrayList<>();

		Creations.setEditable(true);

		// The callback function is called whenever the checked state
		// of the creation changes. The current playlist is then updated accordingly.
		Playlist.setCellValueFactory(creationData -> {
			Creation creation = creationData.getValue();

			if (creation.isChecked()) {
				if (!currentPlaylist.contains(creation)) currentPlaylist.add(creation);
			} else {
				currentPlaylist.remove(creation);
			}

			if (!currentPlaylist.isEmpty()) Video.setMediaPlayer(creationPlayers.get(currentPlaylist.get(0).getName()).get(0));
			return creation.getChecked();
		});

		// Adds a checkbox to correspond to the boolean value in the column
		Playlist.setCellFactory(CheckBoxTableCell.forTableColumn(Playlist));

		// Add an event listener to the list of creations. When it changes, ie when a creation is added
		// or removed, perform the corresponding action to the MediaPlayer dictionary of creations.
		data.addListener((ListChangeListener.Change<? extends Creation> observable) -> {
			while (observable.next()) {
				observable.getRemoved().forEach(creation -> creationPlayers.remove(creation.getName()));

				observable.getAddedSubList().forEach(creation -> {
					String creationName = creation.getName();
					List<MediaPlayer> creationVideos = new ArrayList<>();

					// pass in a File object URI string as the Media object does not accept relative paths
					File[] creationVideoFiles = new File(getCreationDirectory(creation)).listFiles();

					if (creationVideoFiles != null) {
						for (File creationVideoFile : creationVideoFiles) {
							creationVideos.add(new MediaPlayer(new Media(creationVideoFile.toURI().toString())));
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
			if (Video.getMediaPlayer() != null) {
				Video.getMediaPlayer().stop(); // ensures the video starts from the beginning
				Video.getMediaPlayer().play();
			}
		});

		nextCreation.setOnAction(event -> {
			nowPlaying = (nowPlaying + 1) % currentPlaylist.size();
			String nextVideo = currentPlaylist.get(nowPlaying).getName();
			Video.setMediaPlayer(creationPlayers.get(nextVideo).get(0));
		});

		prevCreation.setOnAction(event -> {
			nowPlaying = (nowPlaying - 1) % currentPlaylist.size(); // TODO: FIX!
			String prevVideo = currentPlaylist.get(nowPlaying).getName();
			Video.setMediaPlayer(creationPlayers.get(prevVideo).get(0));
		});

		// Add an event listener to the Delete button such that when clicked, prompts deletion of creation.
		deleteButton.setOnAction(event -> {
			if (Video.getMediaPlayer() != null) {
				// Gets the file for the currently playing video
				String currentVideo = Video.getMediaPlayer().getMedia().getSource();
				File currentVideoFile = new File(currentVideo);

				// Gets the file's creation name by looking at parent directory
				String creationName = currentVideoFile.getAbsoluteFile().getParentFile().getName();
				Creation creation = data.get(data.indexOf(new Creation(creationName)));

				Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
				confirmDelete.setHeaderText("Delete Creation");
				confirmDelete.setContentText("Are you sure you want to delete " + creationName + "?");

				confirmDelete.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
					List<MediaPlayer> creationPlayerList = creationPlayers.get(creationName);
					for (MediaPlayer creationPlayer : creationPlayerList) {
						creationPlayer.dispose(); // release the videos associated with the creation
					}

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
				Video.getMediaPlayer().stop();
				Video.getMediaPlayer().play();

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