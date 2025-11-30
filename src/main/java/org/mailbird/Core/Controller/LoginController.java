package org.mailbird.Core.Controller;


import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Core.util.Popup;
import org.mailbird.Main;

import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private HBox box_with_connect_button;

    @FXML
    private ResourceBundle resources;

    @FXML
    private Button connect_button;

    @FXML
    private TextField email_input;

    @FXML
    private TextField host_input;

    @FXML
    private TextField passwd_input;

    @FXML
    private TextField port_input;

    @FXML
    private ProgressIndicator spinner;

    private AuthService authService;
    private MailService mailService;

    public void SetAuthService(AuthService authService) {
        this.authService = authService;
    }
    public void SetMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @FXML
    void initialize() {
        connect_button.setOnAction(event -> {
            if (!this.validateFields()) {
                return;
            }

             this.authService.SetUserCredentials(
                     email_input.getText(),
                     passwd_input.getText(),
                     host_input.getText(),
                     port_input.getText(),
                     "imaps"
             ); // imaps by default

            // show spinner
            connect_button.setVisible(false);
            spinner.setVisible(true);

            // try to connect
            try {
                this.mailService.Connect(this.authService.getCredentials());
            } catch (MessagingException e) {
                // disconnect if failed
                try {
                    this.mailService.Disconnect();
                } catch (MessagingException e1) {
                    System.out.println(e1.getMessage());
                }

                // show that login failed
                new Popup(
                        Alert.AlertType.ERROR,
                        "Login Failed",
                        "Could not connect to the mail server. Please check your credentials and try again."
                ).Show();
            }

            // move to the main controller
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("main.fxml"));
                loader.setControllerFactory(param -> new MainController(authService, mailService));
                Parent parent = loader.load();

                Scene scene = new Scene(parent);
                stage.setScene(scene);
                stage.setTitle("MailBird");
                stage.show();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private Boolean validateFields() {
        if (email_input.getText().isEmpty()) {
            email_input.setStyle("-fx-border-color: red;");
            return false;
        }
        if (!validateEmail(email_input.getText())) {
            email_input.setStyle("-fx-border-color: red;");
            return false;
        }

        if (host_input.getText().isEmpty()) {
            host_input.setStyle("-fx-border-color: red;");
            return false;
        }

        if (passwd_input.getText().isEmpty()) {
            passwd_input.setStyle("-fx-border-color: red;");
            return false;
        }

        if (port_input.getText().isEmpty()) {
            port_input.setStyle("-fx-border-color: red;");
            return false;
        }

        return true;
    }

    private Boolean validateEmail(String email) {
        return (email.contains(".") && email.contains("@"));
    }
}
