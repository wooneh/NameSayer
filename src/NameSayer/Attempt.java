package NameSayer;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Objects of this class are the user's attempts at saying a Creation.
 * It contains the AudioClip and Filename associated with the attempt.
 */
public class Attempt {
	/**
	 * The AudioClip associated with the attempt.
	 */
	private AudioClip _audioClip;

	/**
	 * The file name associated with the AudioClip.
	 */
	private File _file;

	public Attempt(String file) {
		_file = new File(file);

		try {
			_audioClip = Applet.newAudioClip(_file.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public AudioClip getClip() {
		return _audioClip;
	}

	public File getFile() {
		return _file;
	}

	@Override
	public String toString() {
		return _file.getName();
	}
}
