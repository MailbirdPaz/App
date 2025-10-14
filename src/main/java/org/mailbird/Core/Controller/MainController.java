package org.mailbird.Core.Controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.mailbird.Core.Entity.Mail;
import org.mailbird.Main;

public class MainController {
    @FXML
    private TextFlow mail_body;

    @FXML
    private ListView<Mail> mail_list;

    @FXML
    private Label mail_title;

    @FXML
    private Button button_settings;

    @FXML
    void onSearchTextChange(ActionEvent event) {

    }

    @FXML
    void initialize() {
        button_settings.setOnAction(event -> {

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            try {
                Main.SwitchScene(stage, "settings.fxml", true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }
}
