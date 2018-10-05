package NameSayer.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static NameSayer.Main.*;

/**
 * This is a task class whose main purpose is to record audio rom the user using ffmpeg.
 * The constructor takes a single input, fileName which is used to name the file.
 */
public class RecordAudio extends Task<Void> {
	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> recordAudioCommand = new ArrayList<>();
		recordAudioCommand.add(SHELL);
		recordAudioCommand.add(COMMAND);

		if (OS.equals("Windows")) recordAudioCommand.add("ffmpeg -f dshow -y -i audio=\"Microphone (Realtek High Definition Audio)\" -t 5 " + TEMP + "/UnsavedAttempt.wav");
		else recordAudioCommand.add("ffmpeg -f alsa -y -i default -t 5 " + TEMP + "/UnsavedAttempt.wav");

		ProcessBuilder createAudio = new ProcessBuilder(recordAudioCommand);
		Process createAudioProcess = createAudio.start();
		createAudioProcess.waitFor();

		return null;
	}
}
