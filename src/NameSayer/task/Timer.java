package NameSayer.task;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.text.Text;

public class Timer extends Task<Void> {
	private Text _clockLabel;

	public Timer(Text clockLabel) {
		_clockLabel = clockLabel;
	}

	public Void call() {
		for (int i = 5; i > 0; i--) {
			int currentCount = i;
			Platform.runLater(() -> _clockLabel.setText(Integer.toString(currentCount)));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
