package org.mailbird.Core.Controller;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import jakarta.mail.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import lombok.Setter;
import org.mailbird.Core.DAO.MailDAO;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Main;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainController {

    @FXML
    private Button button_delete_mail;

    @FXML
    private Button button_new_mail;

    @FXML
    private Button button_settings;

    @FXML
    private Button button_sync;

    @FXML
    private Text from_text;

    @FXML
    private TextField input_search_mail;

    @FXML
    private ListView<Mail> mail_list;

    @FXML
    private Label mail_title;

    @FXML
    private Text to_text;

    @FXML
    private Text when_text;


    @FXML
    private WebView webviewMail;
    private WebEngine web;

    @Setter
    private AuthService authService;
    @Setter
    private MailService mailService;

    private final Button buttonLoadMoreMails = new Button("more...");
    private final ObservableList<Mail> displayedMails = FXCollections.observableArrayList();
    private final int flushLoadMax = 10; // load maximum 10 mails at once

    public MainController(AuthService authService, MailService mailService) {
        this.authService = authService;
        this.mailService = mailService;
    }

    // loads 10 mails and add to the list
    private ObservableList<Mail> LoadMoreMails() throws MessagingException, IOException {
        Message[] messages = this.mailService.LoadMails(this.displayedMails.size(), this.flushLoadMax);
        // save in database
        List<MailEntity> entities = this.mailService.SaveToDatabase(messages);

        // cast to the MailEntity List to display
        ObservableList<Mail> items = FXCollections.observableList(new ArrayList<>());
        for (MailEntity entity : entities) {
            items.addLast(new Mail(entity));
        }

        System.out.println("items: " + items.size());

        // this function pipeline:
        // 1. load mails as [Message] objects from mail server
        // 2. save Message List to the database and return MailEntity List from 'SaveToDatabase'
        // 3. Cast MailEntity List to the ObservableList<Mail>, because javafx lists demands ObservableList<Mail>

        return items;
    }

    public void updateMailsList(ObservableList<Mail> mails) {
        if (mails == null) return;
        this.displayedMails.addAll(mails);
        mail_list.setItems(this.displayedMails);
    }

    private void loadMailsInThread() {
        Task<ObservableList<Mail>> loadMailTask = new Task<>() {
            @Override
            protected ObservableList<Mail> call() throws Exception {
                System.out.println("loading mails...");
                mailService.OpenFolder();
                ObservableList<Mail> mails = LoadMoreMails();

                return mails;
            }
        };

        loadMailTask.setOnSucceeded(event -> {
            mailService.CloseFolder();
            ObservableList<Mail> mails = loadMailTask.getValue();
            updateMailsList(mails);
        });

        loadMailTask.setOnFailed(event -> {
            mailService.CloseFolder();
            loadMailTask.getException().printStackTrace();
        });

        new Thread(loadMailTask).start();
    }

    private void loadMailsFromDatabase() {
        List<Mail> mails =  this.mailService.ListFromDatabase();
        updateMailsList(FXCollections.observableList(mails));
    }

    @FXML
    void initialize() {
        // load mails from server in other thread
        this.loadMailsInThread();

        // while new mails are loading, load saved mails from the database
        this.loadMailsFromDatabase();

        web = webviewMail.getEngine();
        web.setJavaScriptEnabled(false); // secure

        // click on the mail -> show content
//        mail_list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
//            if (newV != null) showMail(newV);
//        });
        mail_list.setOnMouseClicked(e -> {
            Mail m = mail_list.getSelectionModel().getSelectedItem();
            if (m != null) {
                System.out.println("Mail clicked: " + m.subject());
                // show mail content
                showMail(m);

                // show metadata
                mail_title.setText(m.subject() == null ? "(No subject)" : m.subject());
            }
        });


        // Settings button
        button_settings.setOnAction(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            try {
                Main.SwitchScene(stage, "settings.fxml", true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        // search
        this.input_search_mail.setOnAction(event -> {
            System.out.println(((TextField) event.getSource()).getText());
        });

        this.mail_list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Mail item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                try {
                    int index = getIndex();
                    int lastIndex = mail_list.getItems().size() - 1;

                    if (index == lastIndex) {
                        setGraphic(buttonLoadMoreMails);
                    } else {
                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("mail_item.fxml"));
                        Parent cellRoot = loader.load();
                        MailItemController controller = loader.getController();
                        controller.setData(item);
                        setGraphic(cellRoot);
                    }
                } catch (IOException | MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // button "more..."
        buttonLoadMoreMails.setOnAction(e -> {
            this.loadMailsInThread();
        });
    }

    // mail content render
    private void showMail(Mail mail) {
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
