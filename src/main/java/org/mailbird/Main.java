package org.mailbird;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mailbird.Core.Controller.LoginController;
import org.mailbird.Core.DAO.FolderDAO;
import org.mailbird.Core.DAO.MailDAO;
import org.mailbird.Core.DAO.UserDao;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.FolderService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Core.util.Config;
import org.mailbird.Core.util.Hibernate;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
    private Hibernate hibernate;
    private Config config;
    private AuthService authService;
    private MailService mailService;
    private FolderService folderService;
    public static Stage mainStage;

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // save stage, to use it in other controllers
        mainStage = stage;

        // Load .env config
        this.config = new Config();

        // Connect to the database
        this.hibernate = new Hibernate(config.DatabaseUrl);

        // Create user dao and test insert
        UserDao userDao = new UserDao(this.hibernate.getSessionFactory());

        // Auth service
        this.authService = new AuthService(userDao);

        // Mail service
        this.mailService = new MailService(new MailDAO(this.hibernate.getSessionFactory()));

        // Folder service
        this.folderService = new FolderService(new FolderDAO(this.hibernate.getSessionFactory()));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        // di, to be able to use services in the initialize method
        fxmlLoader.setControllerFactory(type -> new LoginController(authService, mailService, folderService));
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("MailBird");
        stage.show();
    }

    public static void Close() {

    }
}

