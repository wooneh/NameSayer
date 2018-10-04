package NameSayer;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.applet.AudioClip;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Creation class is a class for the Creation object.
 * A creation has a single field, that is a Name.
 */
public class Creation implements Comparable<Creation>{
	private final SimpleStringProperty _name;
	private String[] _nameParts;
	private List<Attempt> _attempts;
	private final static SimpleIntegerProperty numCreationsThatHaveAttempts = new SimpleIntegerProperty(0);

    public Creation(String name) {
		_name = new SimpleStringProperty(name);
		_nameParts = name.split("[ -]");
		_attempts = new ArrayList<>();
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

	public static SimpleIntegerProperty getNumCreationsThatHaveAttempts() {
    	return numCreationsThatHaveAttempts;
	}

	public List<Attempt> getAttempts() {
    	return _attempts;
	}

}
