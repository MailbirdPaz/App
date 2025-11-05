package org.mailbird.Core.util;

import javafx.scene.control.Alert;

public class Popup {
    Alert alert;

    public Popup(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        this.alert = alert;
    }

    public void Show() {
        this.alert.showAndWait();
    }
}
