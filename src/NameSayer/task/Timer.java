package NameSayer.task;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

/**
 * This task counts down from 5 seconds when the user starts recording audio, and updates the countdown label.
 */
public class Timer extends Task<Void> {
	/**
	 * Progress bar representing the time left for the recording.
	 */
	private ProgressBar _progress;

	/**
	 * Constructor to tell the Timer which progress bar to use
	 * @param progress progress bar to use
	 */
	public Timer(ProgressBar progress) {
		_progress = progress;
	}

	public Void call() {
		for (int i = 1; i <= 20; i++) {
			double currentProgress = i;
			Platform.runLater(() -> _progress.setProgress(currentProgress / 20));

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
