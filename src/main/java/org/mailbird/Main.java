package org.mailbird;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mailbird.Core.Controller.LoginController;
import org.mailbird.Core.DAO.UserDao;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.util.Config;
import org.mailbird.Core.util.Hibernate;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
    private Hibernate hibernate;
    private Config config;
    private AuthService authService;

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Load .env config
        this.config = new Config();

        // Connect to the database
        this.hibernate = new Hibernate(config.DatabaseUrl);

        // Create user dao and test insert
        UserDao userDao = new UserDao(this.hibernate.getSessionFactory());

        // Auth service
        this.authService = new AuthService(userDao);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent parent = fxmlLoader.load();
        LoginController loginController = fxmlLoader.getController();
        loginController.SetAuthService(this.authService);
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
            fxmlLoader.getController();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("MailBird");
            stage.show();
        }
    }

    public static void Close() {

    }
}

