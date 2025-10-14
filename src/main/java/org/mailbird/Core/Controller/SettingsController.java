package org.mailbird.Core.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SettingsController {
    @FXML
    private Button button_change_account;

    @FXML
    private Button button_close;

    @FXML
    private Button button_log_out;

    @FXML
    private Button button_toggle_theme;

    @FXML
    void initialize() {
        button_close.setOnAction(e -> {

        });
    }
}
