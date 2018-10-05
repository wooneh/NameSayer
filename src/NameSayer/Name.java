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
	private PriorityQueue<Version> _versions = new PriorityQueue<>();
	private String _name;
	private static Map<String, Name> allNames = new HashMap<>();

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
	 * Called on start-up. Creates a database of recordings from the Names Corpus.
	 */
	public static void setAllNames() {
		File[] nameAudioFiles = new File(NAMES_CORPUS).listFiles(); // folder containing database
		if (nameAudioFiles != null) for (File nameAudioFile : nameAudioFiles) new Name(nameAudioFile.getName()); // create database
	}
}
