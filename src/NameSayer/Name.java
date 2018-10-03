package NameSayer;

import java.util.ArrayList;
import java.util.List;

public class Name {
	private List<Version> _versions;

	public Name(String file) {
		_versions = new ArrayList<>();
		addVersion(file);
	}

	public void addVersion(String version) {
		_versions.add(new Version(version));
	}

	public List<Version> getVersions() {
		return _versions;
	}
}
