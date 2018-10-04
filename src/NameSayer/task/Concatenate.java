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
		concatenateCommand.add("ffmpeg -y -f concat -i concatenatedFiles.txt -c copy concatenated.wav");

		ProcessBuilder concatenateAudio = new ProcessBuilder(concatenateCommand);
		Process concatenateAudioProcess = concatenateAudio.start();
		concatenateAudioProcess.waitFor();
		concatenateAudioProcess.destroy();

		List<String> trimAudioCommand = new ArrayList<>();
		trimAudioCommand.add(SHELL);
		trimAudioCommand.add(COMMAND);
		trimAudioCommand.add("ffmpeg -y -i concatenated.wav -af \"silenceremove=0:0:0:-1:0.5:-50dB\" silenced.wav");

		ProcessBuilder trimAudio = new ProcessBuilder(trimAudioCommand);
		Process trimAudioProcess = trimAudio.start();
		trimAudioProcess.waitFor();
		trimAudioProcess.destroy();

		List<String> normalizeCommand = new ArrayList<>();
		normalizeCommand.add(SHELL);
		normalizeCommand.add(COMMAND);
		normalizeCommand.add("ffmpeg -y -i silenced.wav -af \"dynaudnorm=f=75:g=15\" normalized.wav");

		ProcessBuilder normalizeAudio = new ProcessBuilder(normalizeCommand);
		Process normalizeAudioProcess = normalizeAudio.start();
		normalizeAudioProcess.waitFor();

		return null;
	}
}
