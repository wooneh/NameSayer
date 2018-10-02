package NameSayer.task;

import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This task concatenates the audio files specified in concatenatedFiles.txt
 */
public class Concatenate extends Task<Void> {

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> concatenateCommand = new ArrayList<>();
		concatenateCommand.add("/bin/bash");
		concatenateCommand.add("-c");
		concatenateCommand.add("ffmpeg -y -f concat -i concatenatedFiles.txt -c copy concatenated.wav");

		ProcessBuilder concatenateAudio = new ProcessBuilder(concatenateCommand);
		Process concatenateAudioProcess = concatenateAudio.start();
		concatenateAudioProcess.waitFor();
		return null;
	}
}
