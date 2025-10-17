package org.mailbird.Core.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.mailbird.Adapter.POP3;
import org.mailbird.Core.Port.IMail;
import org.mailbird.Main;

import java.io.IOException;
import java.util.Properties;
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

    @FXML
    void initialize() {
        connect_button.setOnAction(event -> {
            System.out.println("Click!");

            if (email_input.getText().isEmpty()) {
                email_input.setStyle("-fx-border-color: red;");
                return;
            }
            if (!validateEmail(email_input.getText())) {
                email_input.setStyle("-fx-border-color: red;");
                return;
            }

            if (host_input.getText().isEmpty()) {
                host_input.setStyle("-fx-border-color: red;");
                return;
            }

            if (passwd_input.getText().isEmpty()) {
                passwd_input.setStyle("-fx-border-color: red;");
                return;
            }

            if (port_input.getText().isEmpty()) {
                port_input.setStyle("-fx-border-color: red;");
                return;
            }
            try {
                // try to connect
                // set info in the properties

//                Main.SwitchScene(stage, "main.fxml", false);

                // show spinner
                connect_button.setVisible(false);
                spinner.setVisible(true);

                    try {
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
                        Parent parent = fxmlLoader.load();

                        // TODO: show spinner in the separate thread
                        MainController mainController = fxmlLoader.getController();
                        mainController.connect(email_input.getText(), passwd_input.getText(), host_input.getText());

                        Scene scene = new Scene(parent);
                        stage.setScene(scene);
                        stage.setTitle("MailBird");
                        stage.show();

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
//
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private Boolean validateEmail(String email) {
        return (email.contains(".") && email.contains("@"));
    }
}
