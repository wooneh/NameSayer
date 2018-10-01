package NameSayer.task;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public class SceneChanger extends Task<Void> {
	private FXMLLoader _loader;
	private Button _button;

	public SceneChanger(Button button, FXMLLoader loader) {
		_loader = loader;
		_button = button;
	}

	@Override
	protected Void call() throws IOException {
		_button.getScene().setRoot(_loader.load());
		return null;
	}
}
