package NameSayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static NameSayer.Main.*;

/**
 * Objects of this class represents a Name in the database.
 * A name can contain one or more recordings.
 */
public class Name {
	/**
	 * Contains a priority queue for the different recordings of this name. Good recordings have priority.
	 */
	private PriorityQueue<Version> _versions = new PriorityQueue<>();

	/**
	 * Name of this object as a String.
	 */
	private String _name;

	/**
	 * Contains a map of the String versions of all names in the database.
	 */
	private static Map<String, Name> allNames = new HashMap<>();

	/**
	 * Constructor for this Name. Takes a file name representing a recording of a name, and parses the name from that.
	 * If the name already exists in the database, the recording is added to the existing name. Otherwise, a new name is created.
	 * @param file filename representing a recording of a name,
	 */
	public Name(String file) {
		String name = file.split("_")[3].toLowerCase(); // split filename
		String newName = name.substring(0, name.length() - 4); // remove extension
		_name = newName;

		if (getAllNames().containsKey(newName)) getAllNames().get(newName).addVersion(file); // add recording to existing name
		else { // create name and add recording
			allNames.put(newName, this);
			addVersion(file);
		}
	}

	/**
	 * Adds a new recording to the name.
	 * @param version filename for this recording
	 */
	private void addVersion(String version) {
		_versions.add(new Version(version));
	}

	public String getName() {
		return _name;
	}

	public PriorityQueue<Version> getVersions() {
		return _versions;
	}

	public static Map<String, Name> getAllNames() {
		return allNames;
	}

	/**
	 * Empties the database of names.
	 */
	public static void clearAllNames() {
		allNames.clear();
	}

	/**
	 * Called on start-up. Creates a database of recordings from the Names Corpus.
	 */
	public static void setAllNames() {
		File[] nameAudioFiles = new File(NAMES_CORPUS).listFiles(); // folder containing database
		if (nameAudioFiles != null) for (File nameAudioFile : nameAudioFiles) new Name(nameAudioFile.getName()); // create database
	}
}
