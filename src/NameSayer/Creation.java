package NameSayer;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;
import java.util.List;
import static NameSayer.Main.CLASSES;

/**
 * A creation is made up of nameParts, separated by either a hyphen or space.
 * A creation can have zero or more attempts by the user.
 */
public class Creation implements Comparable<Creation>{
	private final SimpleStringProperty _name = new SimpleStringProperty("");
	private String[] _nameParts;
	private List<Attempt> _attempts;
	private final static SimpleIntegerProperty numCreationsThatHaveAttempts = new SimpleIntegerProperty(0);
	private static List<Creation> allCreations = new ArrayList<>();

	/**
	 * This constructor creates a fresh creation.
	 * @param name The full name of the creation
	 */
	public Creation(String name) {
		_name.set(name);
		_attempts = new ArrayList<>();
		_nameParts = name.split("[ -]");
		allCreations.add(this);
	}

	/**
	 * This constructor takes a file string which represents a past user attempt.
	 * The string is parsed to get the name corresponding to a creation.
	 * @param file filename for the recording
	 * @param currentCourse course code associated with the creation
	 */
    public Creation(String file, String currentCourse) {
		String[] splitFile = file.split("_"); // split filename
		if (splitFile.length > 1) _name.set(splitFile[1].substring(0, splitFile[1].length() - 4)); // ignore the .txt
		if (allCreations.contains(this)) allCreations.get(allCreations.indexOf(this)).addAttempt(CLASSES + "/" + currentCourse + "/" + file);
    }

    public String getName() {
        return _name.get();
    }

    public String[] getNameParts() {
    	return _nameParts;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Creation)) return false;

        Creation c = (Creation) o;
        return this.getName().toLowerCase().equals(c.getName().toLowerCase());
    }

    public int compareTo(Creation x) {
    	return this.getName().toLowerCase().compareTo(x.getName().toLowerCase());
	}

	public void addAttempt(String attempt) {
		if (_attempts.isEmpty()) numCreationsThatHaveAttempts.set(numCreationsThatHaveAttempts.get() + 1);
		_attempts.add(new Attempt(attempt));
	}

	public void removeAttempt(Attempt attempt) {
    	_attempts.remove(attempt);
    	if (_attempts.isEmpty()) numCreationsThatHaveAttempts.set(numCreationsThatHaveAttempts.get() - 1);
	}

	/**
	 * This method counts the total number of creations that have been inputted that have attempts
	 * @return number of creations that have zero or more attempts.
	 */
	public static SimpleIntegerProperty getNumCreationsThatHaveAttempts() {
    	return numCreationsThatHaveAttempts;
	}

	public static void clearAlLCreations() {
		numCreationsThatHaveAttempts.set(0);
		allCreations.clear();
	}

	public List<Attempt> getAttempts() {
    	return _attempts;
	}

}