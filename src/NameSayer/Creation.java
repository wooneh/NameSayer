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
	private final SimpleBooleanProperty _checked;

    public Creation(String name) {
		_name = new SimpleStringProperty(name);
		_checked = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return _name.get();
    }

    public ObservableValue<Boolean> getChecked() {
    	return _checked;
	}

	public boolean isChecked() {
    	return _checked.get();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Creation)) return false;

        Creation c = (Creation) o;
        return this.getName().equals(c.getName());
    }

    public int compareTo(Creation x) {
    	return this.getName().compareTo(x.getName());
	}

}
