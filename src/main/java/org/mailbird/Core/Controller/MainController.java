package org.mailbird.Core.Controller;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import lombok.Setter;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.Services.MailService;
import org.mailbird.Core.domain.enums.ContainerState;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.interfaces.IMailWriterHandlers;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Core.util.Popup;
import org.mailbird.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private Button button_delete_mail;

    @FXML
    private Button button_new_mail;

    @FXML
    private Button button_settings;

    @FXML
    private Button button_reply_mail;

    @FXML
    private Button button_sync;

    @FXML
    private TextField input_search_mail;

    @FXML
    private ListView<Mail> mail_list;

    @FXML
    private StackPane container;
    private final ObjectProperty<ContainerState> containerState =
            new SimpleObjectProperty<>(ContainerState.READ); // use Objectproperty to have an ability to listen changes

    @Setter
    private AuthService authService;
    @Setter
    private MailService mailService;

    private Mail selectedMail;

    private Parent mailViewerFxml;
    private MailViewerController mailViewerController;

    private Parent mailWriterFxml;
    private MailWriterController mailWriterController;

    private final Button buttonLoadMoreMails = new Button("more...");
    private final ObservableList<Mail> displayedMails = FXCollections.observableArrayList();
    private final int flushLoadMax = 10; // load maximum 10 mails at once

    public MainController(AuthService authService, MailService mailService) {
        this.authService = authService;
        this.mailService = mailService;
    }

    // loads 10 mails and add to the list
    private ObservableList<Mail> LoadMoreMails(long lastUid) throws MessagingException, IOException {
        Message[] messages = this.mailService.LoadMails(this.flushLoadMax, lastUid);
        // TODO: remove
        System.out.println("Loaded messages from the server: " + messages.length);

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
        // but better to use LinkedList to add elements to the start, but since ObservableList does not work with LinkedList
        this.displayedMails.addAll(0, mails);
        this.mail_list.setItems(this.displayedMails);
    }

    private void loadMailsInThread(long lastUid) {
        Task<ObservableList<Mail>> loadMailTask = new Task<>() {
            @Override
            protected ObservableList<Mail> call() throws Exception {
                System.out.println("loading mails...");
                mailService.OpenFolder();

                ObservableList<Mail> mails = LoadMoreMails(lastUid);

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

    private void loadReadView() {
        if (this.mailViewerController == null) return;
        this.container.getChildren().setAll(this.mailViewerFxml);
    }

    private void loadWriteView() {
        if (this.mailWriterController == null) return;
        this.container.getChildren().setAll(this.mailWriterFxml);
    }

    @FXML
    void initialize() throws IOException {
        // try to load mails from database. If database is empty, load from server.
        // If not empty, then load mails from the server, where mail id > the first (earliest) mail from db;
        List<Mail> mails =  this.mailService.ListFromDatabase();
        if (mails.isEmpty()) {
            // load mails from server in other thread
            this.loadMailsInThread(0); // 0 means load the 10 latest  mails
        } else {
            Mail latestMail = this.findNewestMail(mails);
            if (latestMail != null)
            {
                // load mails from server in other thread
                this.loadMailsInThread(latestMail.id()); // load mails after the latest mail from db
            }

            updateMailsList(FXCollections.observableList(mails));
        }

        // load two subscenes
        Platform.runLater(() -> {
            try {
                FXMLLoader viewerLoader = new FXMLLoader(Main.class.getResource("mail_viewer.fxml"));
                this.mailViewerFxml = viewerLoader.load();
                this.mailViewerController = viewerLoader.getController();

                FXMLLoader writerLoader = new FXMLLoader(Main.class.getResource("mail_writer.fxml"));
                this.mailWriterFxml = writerLoader.load();
                this.mailWriterController = writerLoader.getController();
                setMaiLWriterHandlers();

                loadReadView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // pass mail to the viewer when some element in the list clicked
        mail_list.setOnMouseClicked(e -> {
            Mail m = mail_list.getSelectionModel().getSelectedItem();
            if (m != null) {
                System.out.println("Mail clicked: " + m.subject());
                this.selectedMail = m;

                // check if container state is viewer
                if (this.containerState.get() != ContainerState.READ) {
                    this.containerState.set(ContainerState.READ);
                }

                // show mail content
                this.mailViewerController.showMail(m);
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
            this.loadMailsInThread(0);
        });

        this.button_new_mail.setOnAction(e -> {
            this.containerState.set(ContainerState.WRITE);
        });

        this.button_reply_mail.setOnAction(e -> {
            if (this.selectedMail == null) {
                return;
            }

            this.mailWriterController.setReplyMessage(selectedMail);
            this.containerState.set(ContainerState.WRITE);
        });

        this.button_sync.setOnAction(e -> {
            Mail newest = this.findNewestMail(this.displayedMails);
            if (newest != null)
                // TODO: show notfication is no new mails
                this.loadMailsInThread(newest.id());
        });

        this.button_delete_mail.setOnAction(e -> {
            if (this.selectedMail == null) {
                return;
            }

            try {
                // delete mail from server
                mailService.DeleteMail(this.selectedMail.id());
                // delete mail from the list and update it
                this.displayedMails.remove(this.selectedMail);
                this.mail_list.setItems(this.displayedMails);
            } catch (MessagingException ex) {
                new Popup(Alert.AlertType.ERROR, "Failed to delete mail", "Failed to delete mail '" + this.selectedMail.subject() + "'").Show();
                ex.printStackTrace();
            }
        });

        // change scene inside the container, if value have changed
        this.containerState.addListener((obs, oldVal, newVal) -> {
            System.out.println("State changed from " + oldVal + " to " + newVal);

            if (newVal == ContainerState.READ) {
                loadReadView();
            } else if (newVal == ContainerState.WRITE) {
                loadWriteView();
            }
        });
        }

        private void setMaiLWriterHandlers() {
            this.mailWriterController.setHandlers(new IMailWriterHandlers() {
                @Override
                public void onSend(String to, String subject, String body) {
                    try {
                        System.out.println("Try to send messaeg!");
                        mailService.SendMail(authService.getCredentials(), to, subject, body);
                        new Popup(Alert.AlertType.CONFIRMATION, "Success", "Message '" + subject + "' send!").Show();
                    } catch (MessagingException e) {
                        new Popup(Alert.AlertType.ERROR, "Failed to send message", "Failed to send message").Show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose() {
                    containerState.set(ContainerState.READ);
                }
            });
        }

        private Mail findNewestMail(List<Mail> mails) {
            if (mails.isEmpty()) {
                return null;
            }

            Mail newest = mails.get(0);

            for (Mail mail : mails) {
                if (mail.id() > newest.id()) {
                    newest = mail;
                }
            }

            return newest;
        }
    }