package NameSayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Objects of this class represents a Name in the database.
 * A name can contain one or more recordings.
 */
public class Name {
	private List<Version> _versions = new ArrayList<>();
	private static Map<String, Name> allNames = new HashMap<>();

	public Name(String file) {
		String name = file.split("_")[3].toLowerCase(); // split filename
		String newName = name.substring(0, name.length() - 4); // remove extension

		if (getAllNames().containsKey(newName)) getAllNames().get(newName).addVersion(file); // add recording to existing name
		else { // create name and add recording
			allNames.put(newName, this);
			addVersion(file);
		}
	}

	private void addVersion(String version) {
		_versions.add(new Version(version));
	}

	public List<Version> getVersions() {
		return _versions;
	}

	public static Map<String, Name> getAllNames() {
		return allNames;
	}
}
