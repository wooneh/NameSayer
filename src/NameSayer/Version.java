package NameSayer;

public class Version {
	private String _name;
	private String _fileName;

	/**
	 * This class associates a name in the a database with a filename.
	 * @param fileName complete filename for the name in the database
	 */
	public Version(String fileName) {
		_fileName = fileName;

		String[] splitFile = fileName.split("_");
		String splitName = splitFile[splitFile.length - 1];
		_name = splitName.substring(0, 1).toUpperCase() + splitName.substring(1, splitName.length() - 4);
	}

	@Override
	public String toString() {
		return _name;
	}

	public String getFileName() {
		return _fileName;
	}
}
