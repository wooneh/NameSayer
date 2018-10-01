package NameSayer;

import javafx.beans.property.SimpleStringProperty;
import java.applet.AudioClip;
import java.util.HashMap;
import java.util.Map;

/**
 * The Creation class is a class for the Creation object.
 * A creation has a single field, that is a Name.
 */
public class Creation implements Comparable<Creation>{
	private final SimpleStringProperty _name;
	private String[] _nameParts;

    public Creation(String name) {
		_name = new SimpleStringProperty(name);
		_nameParts = name.split("[ -]");
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
    	// compare the names (case insensitive)
    	return this.getName().toLowerCase().compareTo(x.getName().toLowerCase());
	}

}
