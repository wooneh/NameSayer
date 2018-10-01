package NameSayer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

/**
 * The Creation class is a class for the Creation object.
 * A creation has a single field, that is a Name.
 */
public class Creation implements Comparable<Creation>{
	private final SimpleStringProperty _name;

    public Creation(String name) {
		_name = new SimpleStringProperty(name);
    }

    public String getName() {
        return _name.get();
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
