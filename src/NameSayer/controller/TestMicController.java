package NameSayer.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestMicController implements Initializable {
    private SourceDataLine sourceLine;
    private TargetDataLine targetLine;
    private Thread micThread = new Thread(new Background());

    @FXML
    private Button backButton;

    @FXML
    private ProgressBar soundLevelBar;

    @FXML
    private void backButtonAction(ActionEvent event) throws IOException {
        try {
            backButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/NameSayer.fxml")).load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        micThread.stop();


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open();

            info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open();

        } catch(LineUnavailableException lue) { lue.printStackTrace(); }

        micThread.start();
    }

    private class Background extends Task<Void> {

        public Background() {

        }

        @Override
        protected Void call() throws Exception {
            targetLine.start();

            byte[] data = new byte[targetLine.getBufferSize() / 5];
            int readBytes;
            while (true) {
                readBytes = targetLine.read(data, 0, data.length);

                double max;
                if (readBytes >=0) {
                    max = (double) (data[0] + (data[1] << 8));
                    for (int p=2;p<readBytes-1;p+=2) {
                        double thisValue = (double) (data[p] + (data[p+1] << 8));
                        if (thisValue>max) max=thisValue;
                    }
                    if (max / 10000 < 0 == false) {
                        soundLevelBar.setProgress(max / 10000);
                    }
                }
            }
        }
    }
}
