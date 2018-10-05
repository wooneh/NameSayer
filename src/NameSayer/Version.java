package NameSayer;

/**
 * This class associates a name in the a database with a filename.
 */
public class Version implements Comparable<Version>{
	private Name _name;
	private String _fileName;
	private Rating _rating;

	public Version(String fileName){
		_rating = Rating.GOOD;
		_fileName = fileName;

		String[] splitFile = fileName.split("_");
		String splitName = splitFile[splitFile.length - 1];
		_name = Name.getAllNames().get(splitName.substring(0, splitName.length() - 4).toLowerCase());
	}

	@Override
	public String toString() {
		return _name.getName();
	}

	public int compareTo(Version version) {
		if (this.getRating().equals(Rating.GOOD) && version.getRating().equals(Rating.BAD)) return -1;
		else if (this.getRating().equals(Rating.BAD) && version.getRating().equals(Rating.GOOD)) return 1;
		else return 0;
	}

	private Rating getRating() {
		return _rating;
	}

	public String getFileName() {
		return _fileName;
	}

	public void setRating(Rating rating) {
		_rating = rating;

		if (_name.getVersions().contains(this)) { // refresh the priority queue
			_name.getVersions().remove(this);
			_name.getVersions().add(this);
		}

		if (rating.equals(Rating.BAD)) Rating.addBadRating(this);
		else Rating.removeBadRating(this);
	}
}
