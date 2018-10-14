package NameSayer.task;

import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static NameSayer.Main.*;

/**
 * This task concatenates, normalizes, and removes silence from the audio files specified in concatenatedFiles.txt
 */
public class Concatenate extends Task<Void> {

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> concatenateCommand = new ArrayList<>();
		concatenateCommand.add(SHELL);
		concatenateCommand.add(COMMAND);
		concatenateCommand.add("ffmpeg -y -f concat -safe 0 -i " + TEMP + "/concatenatedFiles.txt -c copy " + TEMP + "/concatenated.wav");
		new ProcessBuilder(concatenateCommand).start().waitFor();

		return null;
	}
}
