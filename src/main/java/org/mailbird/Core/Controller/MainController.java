package org.mailbird.Core.Controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.TextFlow;
import org.mailbird.Core.Entity.Mail;

public class MainController {
    @FXML
    private TextFlow mail_body;

    @FXML
    private ListView<Mail> mail_list;

    @FXML
    private Label mail_title;

    @FXML
    void onSearchTextChange(ActionEvent event) {

    }
}
