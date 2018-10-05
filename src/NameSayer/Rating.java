package NameSayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public enum Rating {
	GOOD, BAD;

	public static final File BAD_RATINGS = new File("BadRatings.txt");
	private static List<String> badRatings = new ArrayList<>();

	public static void setBadRatings() {
		try {
			if (BAD_RATINGS.exists() || BAD_RATINGS.createNewFile()) {
				List<String> badRatings = Files.readAllLines(BAD_RATINGS.toPath(), StandardCharsets.UTF_8);
				new PrintWriter(BAD_RATINGS).close(); // clear the file contents
				for (Name name : Name.getAllNames().values()) {
					List<Version> versions = new ArrayList<>(name.getVersions());
					for (Version version : versions) {
						if (badRatings.contains(version.getFileName())) version.setRating(Rating.BAD);
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getBadRatings() {
		return badRatings;
	}

	public static void addBadRating(Version version) {
		badRatings.add(version.getFileName());

		try {
			FileWriter fileWriter = new FileWriter(BAD_RATINGS, true);
			fileWriter.write(version.getFileName() + System.lineSeparator());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removeBadRating(Version version) {
		badRatings.remove(version.getFileName());

		try {
			List<String> ratingsContent = new ArrayList<>(Files.readAllLines(BAD_RATINGS.toPath(), StandardCharsets.UTF_8));
			ratingsContent.remove(version.getFileName());
			Files.write(BAD_RATINGS.toPath(), ratingsContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
