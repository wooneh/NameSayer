package NameSayer.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static NameSayer.Main.*;

/**
 * This task removes the silence from an attempt, then saves the attempt to its corresponding class folder.
 */
public class SaveAudio extends Task<String> {
	private String _currentCourse;
	private String _creationName;

	public SaveAudio(String currentCourse, String creationName) {
		_currentCourse = currentCourse;
		_creationName = creationName;
	}

	@Override
	protected String call() throws IOException, InterruptedException {
		String timestamp = new Timestamp(new Date().getTime()).toString().replace(':','-');
		File file = new File(CLASSES + "/" + _currentCourse + "/" + timestamp + "_" + _creationName + ".wav");

		List<String> saveAudioCommand = new ArrayList<>();
		saveAudioCommand.add(SHELL);
		saveAudioCommand.add(COMMAND);
		saveAudioCommand.add("ffmpeg -y -i " + TEMP + "/UnsavedAttempt.wav \"" + file + "\"");
		new ProcessBuilder(saveAudioCommand).start().waitFor();

		return file.getPath();
	}
}
