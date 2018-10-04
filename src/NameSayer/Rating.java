package NameSayer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import static NameSayer.Main.*;

public enum Rating {
	GOOD, BAD;

	private static List<String> _badRatings = new ArrayList<>();

	public static void setBadRatings(List<String> badRatings) {
		_badRatings = badRatings;
	}

	public static List<String> getBadRatings() {
		return _badRatings;
	}

	public static void addBadRating(Version version) {
		_badRatings.add(version.getFileName());

		try {
			FileWriter fileWriter = new FileWriter(BAD_RATINGS, true);
			fileWriter.write(version.getFileName() + System.lineSeparator());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removeBadRating(Version version) {
		_badRatings.remove(version.getFileName());

		try {
			List<String> ratingsContent = new ArrayList<>(Files.readAllLines(BAD_RATINGS.toPath(), StandardCharsets.UTF_8));
			ratingsContent.remove(version.getFileName());
			Files.write(BAD_RATINGS.toPath(), ratingsContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
