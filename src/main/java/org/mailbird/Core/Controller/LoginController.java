package org.mailbird.Core.Controller;


import jakarta.mail.MessagingException;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.FolderService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Core.domain.model.User;
import org.mailbird.Core.util.Popup;
import org.mailbird.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private HBox box_users_list;

    @FXML
    private VBox box_users_list_container;

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

    private final AuthService authService;
    private final MailService mailService;
    private final FolderService folderService;

    private String userSelectedEmail = "";

    public LoginController(AuthService authService, MailService mailService, FolderService folderService) {
        this.authService = authService;
        this.mailService = mailService;
        this.folderService = folderService;
    }

    @FXML
    void initialize() {
        if (this.authService == null) {return;}

        // make users list invisible for now
        setUserListVisible(false);

        ArrayList<User> users = this.authService.GetAllUsers();
        if (!users.isEmpty()) {
            loadUserItems(users);
            setUserListVisible(true);
        }

        setConnectHandler();
    }

    private void loadUserItems(ArrayList<User> users) {
        if (users.isEmpty()) {
            return;
        }

            try {
                for (User user : users) {
                    FXMLLoader loader = new FXMLLoader(Main.class.getResource("account_elem.fxml"));
                    Parent fxmlElem = loader.load();
                    UserItemController fxmlController = loader.getController();
                    fxmlController.SetUser(user.email(), () -> {
                        this.userSelectedEmail = user.email();
                    });

                    box_users_list.getChildren().add(fxmlElem);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void setUserListVisible(boolean visible) {
        box_users_list_container.setVisible(visible);
        box_users_list_container.setManaged(visible);
    }

    private void setConnectHandler() {
        connect_button.setOnAction(event -> {
            if (userSelectedEmail.isEmpty()) {
                // if no user selected, log in by input fields
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
            } else {
                this.authService.SetUserByEmail(userSelectedEmail);
            }

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

            // create default folder
            folderService.CreateDefaultFolder(authService.getCurrentUser());

            // move to the main controller
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("main.fxml"));
                loader.setControllerFactory(param -> new MainController(authService, mailService, folderService));
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
