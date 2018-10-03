package NameSayer.controller;

import NameSayer.Creation;
import NameSayer.Version;
import NameSayer.task.Concatenate;
import NameSayer.task.RecordAudio;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javax.sound.sampled.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class NameSayer {
	@FXML VBox body;
	@FXML TableView<Creation> Creations;
	@FXML Button playButton;
	@FXML Button nextCreation;
	@FXML Button prevCreation;
	@FXML Button attemptName;
	@FXML Button playAttempt;
	@FXML Button saveAttempt;
	@FXML Button trashAttempt;
	@FXML ComboBox<Version> Versions;
	@FXML Text currentCreationName;
	@FXML Text lastRecording;
	@FXML HBox attemptButtons;
	@FXML CheckBox badRating;
	@FXML HBox rateRecording;
	@FXML Text currentCourse;
	@FXML ProgressBar soundLevelBar;

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
		practiceNames = practiceNames.stream().distinct().collect(Collectors.toList()); // remove duplicates from list

		try { // course code must be set beforehand
			File classList = new File("classes/" + currentCourse.getText() + "/" + currentCourse.getText() + ".txt");
			Files.write(classList.toPath(), practiceNames, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.setItems(FXCollections.observableArrayList(practiceNames.stream().map(name -> new Creation(name)).collect(Collectors.toList())));
	}

	/**
	 * This method creates a folder (if one doesn't exist) for the inputted class
	 * @param courseCode Course code for the selected class
	 */
	public void setCourseCode(String courseCode) {
		File classFolder = new File("classes/" + courseCode);
		if (classFolder.exists() || classFolder.mkdir()) currentCourse.setText(courseCode);
	}

	public void initialize() {
		Map<String, List<String>> names = new HashMap<>();
		List<String> ratings = new ArrayList<>();
		File ratingFile = new File("ratings.txt");
		File namePath = new File("names");

		if (namePath.exists() || namePath.mkdir()) { // Create the name directory  if one doesn't already exist
			File[] nameAudioFiles = namePath.listFiles();
			if (nameAudioFiles != null) { // Populate table with existing names and load media files
				for (File nameAudioFile : nameAudioFiles) {
					String[] splitFile = nameAudioFile.getName().split("_");
					String name = splitFile[splitFile.length - 1].toLowerCase();
					String newName = name.substring(0, name.length() - 4);

					if (names.containsKey(newName)) names.get(newName).add(nameAudioFile.getName()); // add recording to existing name
					else { // create a new entry in the database and add the name and recording
						List<String> nameRecordings = new ArrayList<>();
						nameRecordings.add(nameAudioFile.getName());
						names.put(newName, nameRecordings);
					}
				}
			}
		}

		try { // loads the ratings for each recording
			if (ratingFile.exists() || ratingFile.createNewFile()) ratings.addAll(Files.readAllLines(ratingFile.toPath(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // new selection can be null if deleted the last creation
				currentCreationName.setText(newValue.getName()); // Shows the name of the current creation in the UI
				Versions.getItems().clear();

				List<String> filesToConcatenate = new ArrayList<>();
				for (String namePart : newValue.getNameParts()) { // Associates a name part with a name from the database
					List<String> clips = names.get(namePart.toLowerCase());
					if (clips != null) {
						filesToConcatenate.add("file 'names/" + clips.get(0) + "'");
						Versions.getItems().add(new Version(clips.get(0)));
					}
				}

				Versions.getSelectionModel().selectFirst();

				try { // writes the files to concatenate to a text file
					Files.write(new File("concatenatedFiles.txt").toPath(), filesToConcatenate, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}

				new Thread(new Concatenate()).start();
			}
		});

		Versions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) { // update rating
				rateRecording.setDisable(false);
				if (ratings.contains(newValue.getFileName())) badRating.setSelected(true);
				else badRating.setSelected(false);
			} else rateRecording.setDisable(true);
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
				String filePath = "classes/" + currentCourse.getText() + "/" + timestamp + "_" + creationName + ".wav";

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

		badRating.selectedProperty().addListener((observable, oldValue, newValue) -> {
			String versionName = Versions.getSelectionModel().getSelectedItem().getFileName();
			try {
				if (newValue && !ratings.contains(versionName)) { // rating changed from good to bad
					FileWriter fileWriter = new FileWriter(ratingFile, true);
					fileWriter.write(versionName + System.lineSeparator());
					fileWriter.close();
					ratings.add(versionName);
				} else if (!newValue && ratings.contains(versionName)) { // rating changed from bad to good
					List<String> ratingsContent = new ArrayList<>(Files.readAllLines(ratingFile.toPath(), StandardCharsets.UTF_8));
					ratingsContent.remove(versionName);
					Files.write(ratingFile.toPath(), ratingsContent, StandardCharsets.UTF_8);
					ratings.remove(versionName);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
}