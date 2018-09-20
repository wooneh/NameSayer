package NameSayer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.media.AudioClip;
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
	@FXML private Button testMicButton;
	@FXML private TextField addNewTextField;
	@FXML private Button randomCreation;
	@FXML private TableColumn<Creation, Boolean> Playlist;
	@FXML private ComboBox<String> Versions;
	@FXML private Text currentCreationName;
	@FXML private Slider ratingSlider;
	@FXML Label wordRating;


	private ObservableList<Creation> data;
	private Map<String, Map<String,AudioClip>> creationPlayers = new HashMap<>();
	private String selectedName;

	public void initialize() {

		// Create rating documentation file
		File ratings = new File("Ratings");
		ratings.mkdir();

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
                ratingSlider.setDisable(false);
                wordRating.setDisable(false);
            }
            else {
            	ratingSlider.setDisable(true);
			}

			// Display rating if text file exists for that creation name
			File folder = new File("./Ratings");
			File[] listOfFiles = folder.listFiles();
			boolean found = false;
			for (File file : listOfFiles) {
				if (file.isFile()) {
					if (selectedName.equals(file.getName())) {
						found = true;
                       try {
                           BufferedReader br = new BufferedReader(new FileReader(file));
                           wordRating.setText(br.readLine());
                           if (data.isEmpty()) {
                           	wordRating.setText("");
						   }
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
					}
				}
			}
			if (found == false) {
				wordRating.setText("No Rating");
				if (data.isEmpty()) {
					wordRating.setText("");
				}
			}

			if (newValue != null) { // new selection can be null if deleted the last creation
				// Stops the old audio from playing
				String oldFile = Versions.getSelectionModel().getSelectedItem();
				if (oldFile != null) creationPlayers.get(oldValue.getName()).get(oldFile).stop();

				// Shows the name of the current creation in the UI
				currentCreationName.setText(newValue.getName());

				Versions.setItems(FXCollections.observableArrayList(creationPlayers.get(newValue.getName()).keySet()));
				Versions.getSelectionModel().select(0);
			} else currentCreationName.setText("Choose Creation");
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
							creationVideos.put(creationVideoFile.getName(), new AudioClip(creationVideoFile.toURI().toString()));
						}
					}

					creationPlayers.put(creationName, creationVideos);
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
			if (creation != null) {
				// Gets the currently selected creation to delete
				String creationName = creation.getName();

				Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
				confirmDelete.setHeaderText("Delete Creation");
				confirmDelete.setContentText("Are you sure you want to delete " + creationName + "?");

				confirmDelete.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
					File creationVersionsDirectory = new File("creations/" + creationName);
					File[] creationVersions = creationVersionsDirectory.listFiles();

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
				// check if creation already exists, and ask whether we should overwrite it. This overwrites the existing creation
				// from the table and its corresponding MediaPlayer, but not the folder containing the creation.
				if (data.contains(newCreation)) {
					Alert confirmOverwrite = new Alert(Alert.AlertType.CONFIRMATION);
					confirmOverwrite.setHeaderText("There is already an existing creation called " + creationName);
					confirmOverwrite.setContentText("Would you like to overwrite the existing creation?");
					confirmOverwrite.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
						data.remove(newCreation);
						data.add(newCreation);
					});
				} else {
					File newCreationDirectory = new File("creations/" + creationName);
					if (newCreationDirectory.mkdir()) data.add(newCreation);
				}
			} else {
				Alert invalidCharacters = new Alert(Alert.AlertType.WARNING);
				invalidCharacters.setHeaderText("Your creation contains invalid characters");
				invalidCharacters.setContentText("Please choose another name for your creation.");
				invalidCharacters.showAndWait();
			}

			addNewTextField.clear();
		});
	}

	private class Background extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			List<String> command = new ArrayList<String>();
			command.add(0, "/bin/bash");
			command.add(1, "-c");
			command.add(2, "ffmpeg -f alsa -i default -t 5 recordOut.wav| chmod +x recordOut.wav");

			try {
				ProcessBuilder builder = new ProcessBuilder(command);
				builder.directory(new File(System.getProperty("user.dir")));
				Process process = builder.start();
				process.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class playBackBackground extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			List<String> command = new ArrayList<String>();
			command.add(0, "/bin/bash");
			command.add(1, "-c");
			command.add(2, "ffplay -nodisp recordOut.wav");

			try {
				ProcessBuilder builder = new ProcessBuilder(command);
				Process process = builder.start();
				process.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@FXML
	private void testMicAction() {



		Alert testingMicBox = new Alert(Alert.AlertType.INFORMATION);
		testingMicBox.setHeaderText("Testing Microphone...");
		testingMicBox.setContentText("Please speak into your microphone");
		Thread thread = new Thread(new Controller.Background());
		thread.start();

		testingMicBox.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
			Alert playTestVoice = new Alert(Alert.AlertType.INFORMATION);
			playTestVoice.setHeaderText("Playing Voice...");
			playTestVoice.setContentText("Can you hear your voice?");
			Thread playBackThread = new Thread(new Controller.playBackBackground());
			playBackThread.start();
			playTestVoice.showAndWait().filter(playBackResponse -> playBackResponse == ButtonType.OK).ifPresent(playBackResponse -> {
				File file = new File(System.getProperty("user.dir") + "/recordOut.wav");
				file.delete();
			});
		});

	}

	@FXML private void ratingSliderAction() {
	    if (ratingSlider.getValue() > 50) {
	    	wordRating.setText("Good");
	    	try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedName), "utf-8"))) {
	    		writer.write("Good");
			} catch (IOException e) {
	    		e.printStackTrace();
			}

			moveTextFiles();
		}
		else {
			wordRating.setText("Bad");
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedName), "utf-8"))) {
				writer.write("Bad");
			} catch (IOException e) {
				e.printStackTrace();
			}

			moveTextFiles();
		}
    }

    private void moveTextFiles() {
		File folder = new File("./");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				file.renameTo(new File("./Ratings/" + selectedName));
			}
		}
	}
}