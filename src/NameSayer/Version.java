package NameSayer;

public class Version {
	private String _name;
	private String _fileName;

	public Version(String fileName) {
		_fileName = fileName;

		String[] splitFile = fileName.split("_");
		String splitName = splitFile[splitFile.length - 1];
		_name = splitName.substring(0, splitName.length() - 4).toLowerCase();
	}

	@Override
	public String toString() {
		return _name;
	}

	public String getFileName() {
		return _fileName;
	}
}
