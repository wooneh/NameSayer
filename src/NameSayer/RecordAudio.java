package NameSayer;

import javafx.concurrent.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a task class whose main purpose is to record audio rom the user using ffmpeg.
 * The constructor takes a single input, fileName which is used to name the file.
 */
public class RecordAudio extends Task<Void> {
	private String _filePath;

	public RecordAudio(String filePath) {
		_filePath = filePath;
	}

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> recordAudioCommand = new ArrayList<>();
		recordAudioCommand.add("/bin/bash");
		recordAudioCommand.add("-c");
		recordAudioCommand.add("ffmpeg -f alsa -y -i default -t 5 \"" + _filePath + "\"");

		//recordAudioCommand.add("CMD");
		//recordAudioCommand.add("/C");
		//recordAudioCommand.add("ffmpeg -f dshow -y -i audio=\"Microphone (Realtek High Definition Audio)\" -t 5 \"" + _filePath + "\"");

		ProcessBuilder createAudio = new ProcessBuilder(recordAudioCommand);
		Process createAudioProcess = createAudio.start();
		createAudioProcess.waitFor();
		return null;
	}
}
