package org.mailbird.Core.Controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UserItemController {
    @FXML
    private VBox box_user;

    @FXML
    private Text text_email;

    private Runnable onClick;

    @FXML
    void onClicked(MouseEvent event) {
        if (this.onClick != null) {
            this.onClick.run();
        }
    }

    public void SetUser(String email, Runnable onClick) {
        // show only part before @
        this.text_email.setText(email.split("@")[0]);
        this.onClick = onClick;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            this.box_user.setStyle("-fx-background-color: #d0d0d0;");
        } else {
            this.box_user.setStyle("-fx-background-color: transparent;");
        }
    }
}
