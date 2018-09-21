package NameSayer;

import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a task class whose main purpose is to generate a waveform from an audio file using ffmpeg.
 * The constructor takes a single input, which is the filename to generate the waveform from.
 */
public class GenerateWaveForm extends Task<Void> {
	private String _filePath;

	public GenerateWaveForm(String filePath) {
		_filePath = filePath;
	}

	@Override
	protected Void call() throws IOException, InterruptedException {
		List<String> generateWaveFormCommand = new ArrayList<>();
		//generateWaveFormCommand.add("/bin/bash");
		//generateWaveFormCommand.add("-c");
		//generateWaveFormCommand.add("ffmpeg -y -i \"" + _filePath + "\" -filter_complex showwavespic=s=320x120 -frames:v 1 waveform.png");

		generateWaveFormCommand.add("CMD");
		generateWaveFormCommand.add("/C");
		generateWaveFormCommand.add("ffmpeg -y -i \"" + _filePath + "\" -filter_complex showwavespic=s=320x120 -frames:v 1 waveform.png");

		ProcessBuilder createAudio = new ProcessBuilder(generateWaveFormCommand);
		Process createAudioProcess = createAudio.start();
		createAudioProcess.waitFor();
		return null;
	}
}
