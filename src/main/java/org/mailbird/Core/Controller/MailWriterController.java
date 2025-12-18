package org.mailbird.Core.Controller;
import jakarta.mail.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import lombok.Setter;
import org.mailbird.Core.domain.interfaces.IMailWriterHandlers;
import org.mailbird.Core.domain.model.Mail;

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

            // clear fields
            this.input_subject.clear();
            this.input_to.clear();
            this.content_editor.setHtmlText("");

            this.handlers.onClose();
        });
    }

    public void setReplyMessage(Mail mail) {
        try {
            this.input_subject.setText("Re: " + mail.subject());
            this.input_to.setText(mail.from());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
