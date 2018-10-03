package NameSayer;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

public class Attempt {
	private AudioClip _audioClip;
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
