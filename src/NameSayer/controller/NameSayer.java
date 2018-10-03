package NameSayer.controller;

import NameSayer.*;
import NameSayer.task.*;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
	@FXML CheckBox badRating;
	@FXML HBox rateRecording;
	@FXML Text currentCourse;
	@FXML ComboBox<Attempt> pastAttempts;
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
		practiceNames = practiceNames.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

		try { // course code must be set beforehand
			File classList = new File("classes/" + currentCourse.getText() + "/" + currentCourse.getText() + ".txt");
			Files.write(classList.toPath(), practiceNames, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.setItems(FXCollections.observableArrayList(practiceNames.stream().map(name -> new Creation(name)).collect(Collectors.toList())));
		Creations.getSelectionModel().selectFirst();

		File[] attemptFiles = new File("classes/" + currentCourse.getText()).listFiles();
		List<Creation> creations = Creations.getItems();
		if (attemptFiles != null) {
			for (File attemptFile : attemptFiles) {
				String[] splitFile = attemptFile.getName().split("_"); // split filename
				if (splitFile.length > 1) { // ignore the .txt which is the class list
					Creation creation = new Creation(splitFile[1].substring(0, splitFile[1].length() - 4)); // remove extension
					if (creations.contains(creation)) creations.get(creations.indexOf(creation)).addAttempt("classes/" + currentCourse.getText() + "/" + attemptFile.getName());
				}
			}
		}
	}

	/**
	 * This method creates a folder (if one doesn't exist) for the inputted class
	 * @param courseCode Course code for the selected class
	 */
	public void setCourseCode(String courseCode) {
		File classFolder = new File("classes/" + courseCode);
		if (classFolder.exists() || classFolder.mkdir()) currentCourse.setText(courseCode);
	}

	// TODO: Cross out name parts that aren't in the database
	// TODO: Choose recordings for name parts that are not bad quality
	// TODO: Stop audio playing if changing creations/attempts
	// TODO: Disable play button while concatenating audio
	// TODO: Remove silence and equalization from the concatenated audio
	// TODO: Reward system
	// TODO: User help tooltips
	// TODO: Single name input
	public void initialize() {
		File[] nameAudioFiles = new File("names").listFiles(); // folder containing database
		Map<String, Name> names = new HashMap<>(); // names and associated Name Object
		if (nameAudioFiles != null) {
			for (File nameAudioFile : nameAudioFiles) { // add recordings associated with each name
				String name = nameAudioFile.getName().split("_")[3].toLowerCase(); // split filename
				String newName = name.substring(0, name.length() - 4); // remove extension
				if (names.containsKey(newName)) names.get(newName).addVersion(nameAudioFile.getName()); // add recording
				else names.put(newName, new Name(nameAudioFile.getName())); // create name and add recording
			}
		}

		File ratingFile = new File("ratings.txt");
		List<String> ratings = new ArrayList<>(); // recordings that are rated bad
		try { // loads the ratings for each recording
			if (ratingFile.exists() || ratingFile.createNewFile()) ratings.addAll(Files.readAllLines(ratingFile.toPath(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Creations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			nameParts.getItems().clear();
			pastAttempts.setItems(FXCollections.observableArrayList(newValue.getAttempts()));
			if (!pastAttempts.getItems().isEmpty()) pastAttempts.getSelectionModel().selectFirst();

			List<Text> displayCreationName = new ArrayList<>();
			List<String> filesToConcatenate = new ArrayList<>();
			for (String namePart : newValue.getNameParts()) { // Associates a name part with a name from the database
				Text namePartText = new Text(namePart + " ");
				namePartText.setStyle("-fx-font-size: 32px;");

				if (names.containsKey(namePart.toLowerCase())) { // find a recording for the name part
					List<Version> nameVersions = names.get(namePart.toLowerCase()).getVersions();
					filesToConcatenate.add("file 'names/" + nameVersions.get(0).getFileName() + "'");
					nameParts.getItems().add(nameVersions.get(0));
				} else namePartText.setStrikethrough(true);
				displayCreationName.add(namePartText); // cross out name part if recording does not exist in database
			}
			nameParts.getSelectionModel().selectFirst();
			currentCreationName.getChildren().setAll(displayCreationName); // Shows the name of the current creation in the UI

			try { // writes the files to concatenate to a text file
				Files.write(new File("concatenatedFiles.txt").toPath(), filesToConcatenate, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}

			body.setDisable(true);
			Concatenate concatenate = new Concatenate();
			concatenate.setOnSucceeded(finished -> {
				body.setDisable(false);
			});
			new Thread(concatenate).start();
		});

		nameParts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
				body.setDisable(true); // disable UI while recording

				String timestamp = new Timestamp(new Date().getTime()).toString().replace(':','-');
				File filePath = new File("classes/" + currentCourse.getText() + "/" + timestamp + "_" + creationName + ".wav");

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
				if (selectedFile.delete()) {
					pastAttempts.getItems().remove(pastAttempts.getSelectionModel().getSelectedItem());
					if (!pastAttempts.getItems().isEmpty()) pastAttempts.getSelectionModel().selectFirst();
				}
			}
		});

		badRating.selectedProperty().addListener((observable, oldValue, newValue) -> {
			String versionName = nameParts.getSelectionModel().getSelectedItem().getFileName();
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