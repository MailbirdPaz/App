package org.mailbird.Core.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.mailbird.Core.domain.interfaces.IMailWriterHandlers;
import org.mailbird.Core.domain.model.Mail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MailViewerController {
    @FXML
    private Text from_text;

    @FXML
    private Label mail_title;

    @FXML
    private Text to_text;

    @FXML
    private WebView webviewMail;
    private WebEngine web;

    @FXML
    private Text when_text;

    @FXML
    void initialize() {
        web = webviewMail.getEngine();
        web.setJavaScriptEnabled(true); // insecure, but many features will work
    }

    // mail content render
    public void showMail(Mail mail) {
        // show mail metadata
        from_text.setText(mail.from());
        to_text.setText(mail.to());
        when_text.setText(formatEmailDate(mail.date()));

        // show mail content
        String body = mail.body();
        web.loadContent(body, "text/html");
    }

    public static String formatEmailDate(Date date) {
        if (date == null) return "";

        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDate messageDay = dateTime.toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        if (messageDay.isEqual(today)) {
            return "Today, " + dateTime.format(timeFormatter);
        }

        if (messageDay.isEqual(yesterday)) {
            return "Yesterday, " + dateTime.format(timeFormatter);
        }

        return dateTime.format(dateFormatter);  // For older dates
    }
}
