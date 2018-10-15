package NameSayer.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static NameSayer.Main.*;

/**
 * This class's main purpose is to normalize, remove silence, and play an audio file.
 */
public class PlayAudio {
	public static void play(String file) {
		if (!file.isEmpty()) {
			String maxVolume = "0";

			List<String> findMaxVolCommand = new ArrayList<>();
			findMaxVolCommand.add(SHELL);
			findMaxVolCommand.add(COMMAND);

			if (OS.equals("Windows")) findMaxVolCommand.add("ffmpeg -hide_banner -i \"" + file + "\" -af volumedetect -f null NUL");
			else findMaxVolCommand.add("ffmpeg -hide_banner -i \"" + file + "\" -af volumedetect -f null /dev/null");

			List<String> playAudioCommand = new ArrayList<>();
			playAudioCommand.add(SHELL);
			playAudioCommand.add(COMMAND);

			try { // uses the max volume of the audio to normalize all the input volume to
				Process findMaxVol = new ProcessBuilder(findMaxVolCommand).start();

				BufferedReader in = new BufferedReader(new InputStreamReader(findMaxVol.getErrorStream()));
				String line;
				while ((line = in.readLine()) != null) { // reads the max volume of the file
					if (line.contains("max_volume")) {
						Pattern p = Pattern.compile("\\d+\\.\\d+");
						Matcher m = p.matcher(line);
						if (m.find()) maxVolume = m.group(0);
					}
				}

				findMaxVol.waitFor();
				in.close();

				playAudioCommand.add("ffplay -loglevel quiet -nodisp -autoexit -af \"aformat=channel_layouts=mono, silenceremove=0:0:0:-1:0.5:-50dB, volume=" + maxVolume + "dB\" \"" + file + "\"");
				new ProcessBuilder(playAudioCommand).start();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
