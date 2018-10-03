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

		List<String> trimAudioCommand = new ArrayList<>();
		trimAudioCommand.add("/bin/bash");
		trimAudioCommand.add("-c");
		trimAudioCommand.add("ffmpeg -y -i concatenated.wav -af silenceremove=0:0:0:-1:1:-90dB temp.wav");

		ProcessBuilder trimAudio = new ProcessBuilder(trimAudioCommand);
		Process trimAudioProcess = trimAudio.start();
		trimAudioProcess.waitFor();

		List<String> normalizeCommand = new ArrayList<>();
		normalizeCommand.add("/bin/bash");
		normalizeCommand.add("-c");
		normalizeCommand.add("ffmpeg -y -i temp.wav -af dynaudnorm=f=25 concatenated.wav");

		ProcessBuilder normalizeAudio = new ProcessBuilder(normalizeCommand);
		Process normalizeAudioProcess = normalizeAudio.start();
		normalizeAudioProcess.waitFor();

		return null;
	}
}
