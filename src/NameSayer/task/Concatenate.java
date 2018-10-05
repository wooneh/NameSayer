package NameSayer.task;

import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static NameSayer.Main.*;

/**
 * This task concatenates the audio files specified in concatenatedFiles.txt
 */
public class Concatenate extends Task<Void> {

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> concatenateCommand = new ArrayList<>();
		concatenateCommand.add(SHELL);
		concatenateCommand.add(COMMAND);
		concatenateCommand.add("ffmpeg -y -f concat -safe 0 -i " + TEMP + "/concatenatedFiles.txt -c copy " + TEMP + "/concatenated.wav");

		ProcessBuilder concatenateAudio = new ProcessBuilder(concatenateCommand);
		Process concatenateAudioProcess = concatenateAudio.start();
		concatenateAudioProcess.waitFor();

		List<String> normalizeCommand = new ArrayList<>();
		normalizeCommand.add(SHELL);
		normalizeCommand.add(COMMAND);
		normalizeCommand.add("ffmpeg -y -i " + TEMP + "/concatenated.wav -af \"dynaudnorm=f=50:g=15, silenceremove=0:0:0:-1:0.5:-50dB\" " + TEMP + "/normalized.wav");

		ProcessBuilder normalizeAudio = new ProcessBuilder(normalizeCommand);
		Process normalizeAudioProcess = normalizeAudio.start();
		normalizeAudioProcess.waitFor();

		return null;
	}
}
