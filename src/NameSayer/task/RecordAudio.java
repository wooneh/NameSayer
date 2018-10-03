package NameSayer.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a task class whose main purpose is to record audio rom the user using ffmpeg.
 * The constructor takes a single input, fileName which is used to name the file.
 */
public class RecordAudio extends Task<Void> {
	private String _filePath;

	public RecordAudio(File filePath) {
		_filePath = filePath.getPath();
	}

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> recordAudioCommand = new ArrayList<>();
		recordAudioCommand.add("/bin/bash");
		recordAudioCommand.add("-c");
		recordAudioCommand.add("ffmpeg -f alsa -y -i default -t 5 \"" + _filePath + "\"");

//		recordAudioCommand.add("CMD");
//		recordAudioCommand.add("/C");
//		recordAudioCommand.add("ffmpeg -f dshow -y -i audio=\"Microphone (Realtek High Definition Audio)\" -t 5 temp.wav");

		ProcessBuilder createAudio = new ProcessBuilder(recordAudioCommand);
		Process createAudioProcess = createAudio.start();
		createAudioProcess.waitFor();

		List<String> trimAudioCommand = new ArrayList<>();
		trimAudioCommand.add("/bin/bash");
		trimAudioCommand.add("-c");
		trimAudioCommand.add("ffmpeg -i temp.wav -af silenceremove=0:0:0:-1:1:-35dB \"" + _filePath + "\"");

		ProcessBuilder trimAudio = new ProcessBuilder(trimAudioCommand);
		Process trimAudioProcess = trimAudio.start();
		trimAudioProcess.waitFor();

		return null;
	}
}
