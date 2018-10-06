package NameSayer.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		if (OS.equals("Windows")) { // On Windows, get the microphone name we will be using
			List<String> deviceList = new ArrayList<>();
			deviceList.add(SHELL);
			deviceList.add(COMMAND);
			deviceList.add("ffmpeg -list_devices true -f dshow -i dummy > " + TEMP + "/DeviceList.txt 2>&1");

			ProcessBuilder writeFile = new ProcessBuilder(deviceList);
			Process writeFileProcess = writeFile.start();
			writeFileProcess.waitFor();

			File file = new File(TEMP + "/DeviceList.txt");
			String micName = "";
			try {
				List<String> devicesInfo = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
				for (int i = 0; i < devicesInfo.size() - 1; i++) {
					if (devicesInfo.get(i).contains("DirectShow audio devices")) {
						Pattern p = Pattern.compile("\"([^\"]*)\""); // extracts substring between quotes
						Matcher m = p.matcher(devicesInfo.get(i + 1));
						if (m.find()) micName = m.group(1);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			recordAudioCommand.add("ffmpeg -f dshow -y -i audio=\"" + micName + "\" -t 5 " + TEMP + "/UnsavedAttempt.wav");
		}
		else recordAudioCommand.add("ffmpeg -f alsa -y -i default -t 5 " + TEMP + "/UnsavedAttempt.wav");

		ProcessBuilder createAudio = new ProcessBuilder(recordAudioCommand);
		Process createAudioProcess = createAudio.start();
		createAudioProcess.waitFor();

		return null;
	}
}
