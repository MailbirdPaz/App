package org.mailbird.Core.Controller;
import jakarta.mail.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import lombok.Setter;
import org.mailbird.Core.domain.interfaces.IMailWriterHandlers;

public class MailWriterController {
    @FXML
    private Button button_close;

    @FXML
    private Button button_send;

    @FXML
    private HTMLEditor content_editor;

    @FXML
    private TextField input_subject;

    @FXML
    private TextField input_to;

    @Setter
    private IMailWriterHandlers handlers;

    @FXML
    void initialize() {
        this.button_send.setOnAction(e -> {
            if (handlers == null) {
                return;
            }

            if (!validateFields()) {
                return;
            }

           this.handlers.onSend(this.input_to.getText(), this.input_subject.getText(), this.content_editor.getHtmlText());
        });

        this.button_close.setOnAction(e -> {
            if (handlers == null) {
                return;
            }

            this.handlers.onClose();
        });
    }

    private boolean validateFields() {
        if (input_subject.getText().isEmpty()) {
            input_subject.setStyle("-fx-border-color: red");
            return false;
        }

        if (input_to.getText().isEmpty()) {
            input_to.setStyle("-fx-border-color: red");
            return false;
        }

        if (content_editor.getHtmlText().isEmpty()) {
            content_editor.setStyle("-fx-border-color: red");
        }

        return true;
    }
}
