package NameSayer.task;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import javax.sound.sampled.*;

/**
 * The purpose of this task is to capture the user's audio and display a level meter.
 */
public class MicrophoneLevel extends Task<Void> {
	private ProgressBar _progressBar;
	private boolean _isCapturing;

	public MicrophoneLevel() {
		_isCapturing = true;
	}

	public void setCapturing(boolean value) {
		_isCapturing = value;
	}

	public MicrophoneLevel setProgressBar(ProgressBar progressBar) {
		_progressBar = progressBar;
		return this;
	}

	@Override
	protected Void call() {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
			sourceLine.open();

			info = new DataLine.Info(TargetDataLine.class, format);
			TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
			targetLine.open();
			targetLine.start();

			byte[] data = new byte[targetLine.getBufferSize() / 5];
			int readBytes;

			while (_isCapturing) {
				readBytes = targetLine.read(data, 0, data.length);

				double max;
				if (readBytes >= 0) {
					max = (double) (data[0] + (data[1] << 8));
					for (int p = 2; p < readBytes - 1; p += 2) {
						double thisValue = (double) (data[p] + (data[p + 1] << 8));
						if (thisValue>max) max=thisValue;
					}
					if (max / 10000 >= 0) {
						_progressBar.setProgress(max / 10000);
					}
				}
			}

			sourceLine.close();
			targetLine.close();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		return null;
	}
}
