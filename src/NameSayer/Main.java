package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for the NameSayer application.
 * @author Samantha Woon and Clayton Lan, Team 34
 */
public class Main extends Application {
	public static final String NAMES_CORPUS = "names";
	public static final String CLASSES ="classes";
	public static final String TEMP = "temp";

	public static String SHELL = "/bin/bash";
	public static String COMMAND = "-c";
	public static String OS = "Linux";
	public static String MICNAME;

	@Override
	public void start(Stage stage) throws Exception {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			SHELL = "CMD";
			COMMAND = "/C";
			OS = "Windows";

			List<String> deviceList = new ArrayList<>();
			deviceList.add(SHELL);
			deviceList.add(COMMAND);
			deviceList.add("ffmpeg -list_devices true -f dshow -i dummy > " + TEMP + "/DeviceList.txt 2>&1");
			new ProcessBuilder(deviceList).start().waitFor();

			File file = new File(TEMP + "/DeviceList.txt");
			try { // On Windows, get the microphone name we will be using
				List<String> devicesInfo = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
				for (int i = 0; i < devicesInfo.size() - 1; i++) {
					if (devicesInfo.get(i).contains("DirectShow audio devices")) {
						Pattern p = Pattern.compile("\"([^\"]*)\""); // extracts substring between quotes
						Matcher m = p.matcher(devicesInfo.get(i + 1));
						if (m.find()) MICNAME = m.group(1);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		stage.setTitle("NameSayer");
		stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/HomeScreen.fxml"))));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
