package org.mailbird.Core.Controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mailbird.Main;

import java.io.IOException;
import java.util.ResourceBundle;

public class LoginController {

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
    void initialize() {
        connect_button.setOnAction(event -> {
            System.out.println("Click!");

            if (email_input.getText().isEmpty()) {
                email_input.setStyle("-fx-border-color: red;");
            }
            if (!validateEmail(email_input.getText())) {
                email_input.setStyle("-fx-border-color: red;");
            }

            if (host_input.getText().isEmpty()) {
                host_input.setStyle("-fx-border-color: red;");
            }

            if (passwd_input.getText().isEmpty()) {
                passwd_input.setStyle("-fx-border-color: red;");
            }

            if (port_input.getText().isEmpty()) {
                port_input.setStyle("-fx-border-color: red;");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            try {
                Main.SwitchScene(stage, "main.fxml", false);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private Boolean validateEmail(String email) {
        return (email.contains(".") && email.contains("@"));
    }
}
