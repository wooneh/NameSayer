package NameSayer.task;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import static NameSayer.Main.*;

/**
 * This is a task class whose main purpose is to record audio from the user using ffmpeg.
 * The user can control however long they wish to record. The interface shows a countdown of when the recording will end.
 */
public class RecordAudio extends Task<Void>{
	/**
	 * Progress bar representing the time left for the recording.
	 */
	private ProgressBar _progress;

	/**
	 * Time left in the countdown
	 */
	private double _time = 20;

	/**
	 * Constructor to tell the task which progress bar to use
	 * @param progress progress bar to use
	 */
	public RecordAudio(ProgressBar progress) {
		_progress = progress;
	}

	public Void call() throws IOException{
		List<String> recordAudioCommand = new ArrayList<>();
		recordAudioCommand.add(SHELL);
		recordAudioCommand.add(COMMAND);

		if (OS.equals("Windows")) recordAudioCommand.add("ffmpeg -f dshow -y -i audio=\"" + MICNAME + "\" " + TEMP + "/UnsavedAttempt.wav");
		else recordAudioCommand.add("ffmpeg -f alsa -y -i default " + TEMP + "/UnsavedAttempt.wav");

		Process process = new ProcessBuilder(recordAudioCommand).start();

		while (_time > 0) {
			Platform.runLater(() -> _progress.setProgress(1 - _time / 20));
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			_time--;
		}

		try {
			OutputStream outputStream = process.getOutputStream();
			outputStream.write("q".getBytes());
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method restarts the timer so that the user can keep recording.
	 */
	public void restart() {
		_time = 20;
	}

	/**
	 * This method stops the timer so the user stops recording.
	 */
	public void stop() {
		_time = 0;
	}
}
