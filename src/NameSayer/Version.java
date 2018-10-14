package NameSayer;

/**
 * This class associates a name in the a database with a filename.
 */
public class Version implements Comparable<Version>{
	/**
	 * The Name that this recording is associated with
	 */
	private Name _name;

	/**
	 * Audio file of this recording
	 */
	private String _fileName;

	/**
	 * The current rating (GOOD or BAD) of the current recording. Default value is GOOD.
	 */
	private Rating _rating;

	/**
	 * Constructor to create a Version of a Name. Parses the filename for the Name to associate this recording.
	 * @param fileName relative path of the file for this recording
	 */
	public Version(String fileName){
		_rating = Rating.GOOD;
		_fileName = fileName;

		String[] splitFile = fileName.split("_");
		String splitName = splitFile[splitFile.length - 1];
		_name = Name.getAllNames().get(splitName.substring(0, splitName.length() - 4).toLowerCase());
	}

	@Override
	public String toString() {
		return _name.getName().substring(0, 1).toUpperCase() + _name.getName().substring(1);
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

	/**
	 * Sets the rating for this version and updates the Ratings file and list.
	 * @param rating value to set the Rating for this recording to
	 */
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
