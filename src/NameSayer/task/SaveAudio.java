package NameSayer.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static NameSayer.Main.*;

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

		List<String> removeSilenceCommand = new ArrayList<>();
		removeSilenceCommand.add(SHELL);
		removeSilenceCommand.add(COMMAND);
		removeSilenceCommand.add("ffmpeg -y -i " + TEMP + "/UnsavedAttempt.wav -af \"silenceremove=0:0:0:-1:0.5:-50dB\" \"" + file + "\"");

		ProcessBuilder removeSilence = new ProcessBuilder(removeSilenceCommand);
		Process createAudioProcess = removeSilence.start();
		createAudioProcess.waitFor();

		return file.getPath();
	}
}
