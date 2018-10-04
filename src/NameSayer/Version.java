package NameSayer;

/**
 * This class associates a name in the a database with a filename.
 */
public class Version {
	private String _name;
	private String _fileName;
	private Rating _rating;

	public Version(String fileName) {
		_fileName = fileName;
		_rating = Rating.GOOD;

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

	public void setRating(Rating rating) {
		_rating = rating;
		if (rating.equals(Rating.BAD)) Rating.addBadRating(this);
		else Rating.removeBadRating(this);
	}
}
