package org.mailbird.Core.Controller;

import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.mailbird.Core.Entity.Mail;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MailItemController {
    @FXML
    private Label label_date;

    @FXML
    private Label label_from;

    @FXML
    private Label label_subject;

    void setData(Mail mail) throws MessagingException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd / HH:mm", Locale.ENGLISH);

        label_from.setText(mail.from().email());
        label_date.setText(formatter.format(mail.date()));
        label_subject.setText(mail.subject());
    }

    @FXML
    void initialize() {

    }
}
