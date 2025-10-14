package org.mailbird;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("MailBird");
        stage.show();
    }

    public static void SwitchScene(Stage stage, String sceneName, Boolean isSeparateWindow) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(sceneName));
        Parent parent = fxmlLoader.load();

        if (isSeparateWindow) {
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(parent));

            // Optional: make it modal (blocks the main window until closed)
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.show();
        } else {
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("MailBird");
            stage.show();
        }
    }

    public static void Close() {

    }
}