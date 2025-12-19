package org.mailbird.Core.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Setter;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Main;

public class SettingsController {
    @FXML
    private Button button_change_account;

    @FXML
    private Button button_close;

    @FXML
    private Button button_log_out;

    @FXML
    private Button button_toggle_theme;

    private AuthService authService;
    private MailService mailService;

    public SettingsController(AuthService authService, MailService mailService) {
        this.authService = authService;
        this.mailService = mailService;
    }

    @FXML
    void initialize() {
        button_log_out.setOnAction(e -> {
            if (authService == null || mailService == null) {
                System.out.println("Cannot Logout, because services are null in the SettingsController");
                return;
            }

            authService.LogOut();
            mailService.EndSession();

            // close the settings window
            closeWindow(e);

            // move to the login screen
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("login.fxml"));
                loader.setControllerFactory(type -> {
                    if (type == LoginController.class) {
                        return new LoginController(authService, mailService);
                    }
                    try {
                        return type.getDeclaredConstructor().newInstance();
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                Main.mainStage.setScene(scene);
                Main.mainStage.setTitle("MailBird");
                Main.mainStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        button_close.setOnAction(e -> {
            closeWindow(e);
        });
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        stage.close();
    }
}
