package NameSayer;

import java.io.File;

/**
 * Objects of this class are the user's attempts at saying a Creation.
 */
public class Attempt {
	/**
	 * The file name associated with the AudioClip.
	 */
	private File _file;

	public Attempt(String file) {
		_file = new File(file);
	}

	public File getFile() {
		return _file;
	}

	@Override
	public String toString() {
		return _file.getName();
	}
}
