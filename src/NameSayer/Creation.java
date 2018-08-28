package NameSayer;

import javafx.beans.property.SimpleStringProperty;

/**
 * The Creation class is a class for the Creation object.
 * A creation has a single field, that is a Name.
 */
public class Creation {
    private final SimpleStringProperty _name = new SimpleStringProperty("");

    public Creation() {
        this("");
    }

    public Creation(String name) {
        setName(name);
    }

    public void setName(String name) {
        _name.set(name);
    }

    public String getName() {
        return _name.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Creation)) return false;

        Creation c = (Creation) o;
        return this.getName().equals(c.getName());
    }

}
