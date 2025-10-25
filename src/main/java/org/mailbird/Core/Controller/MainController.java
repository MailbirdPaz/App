package org.mailbird.Core.Controller;


import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.mailbird.Adapter.POP3;
import org.mailbird.Core.Entity.Mail;
import org.mailbird.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MainController {
    @FXML
    private TextFlow mail_body;

    @FXML
    private ListView<Mail> mail_list;

    @FXML
    private Label mail_title;

    @FXML
    private Button button_settings;

    @FXML
    private TextField input_search_mail;

    @FXML
    void onSearchTextChange(ActionEvent event) {

    }

    private Button buttonLoadMoreMails = new Button("more...");
    private ObservableList<Mail> displayedMails = FXCollections.observableArrayList();
    private POP3 mailService;
    private Store store;
    private int flushLoadMax = 10; // limit to load maximum 10 mails per loading


    // loads 10 mails and add to the list
    private ObservableList<Mail> LoadMoreMails() throws MessagingException, IOException {
        // OpenFolder method returns an array of Messages. We need to cast from []Messages to the ObservableList<Mail>
        // second argument = offset = how many mails to skip. Is used when some mails are already loaded
        Message[] messages = this.mailService.LoadMails(this.store, this.displayedMails.size(), this.flushLoadMax);
        ObservableList<Mail> items = FXCollections.observableList(new ArrayList<Mail>());
        for (Message msg : messages) {
            items.addLast(new Mail(msg));
        }

        return items;
    }

    // updates local arrays
    public void updateMailsList(ObservableList<Mail> mails) {
        if (mails == null) {
            return;
        }

        System.out.println("Updating mails list");
        for  (Mail mail : mails) {
            System.out.println("Updating mail: " + mail.subject());
        }

        // add mails to the local arrays
        this.displayedMails.addAll(mails);
        // update visual list
        mail_list.setItems(this.displayedMails);
    }

    public void connect(String user, String password, String host) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "pop3s");
        props.put("mail.pop3s.host", host);
        props.put("mail.pop3s.port", "995"); // 995
        props.put("mail.pop3s.ssl.enable", "true");

        System.out.println("Trying to connect to mail service");
        POP3 ms =  new POP3();
        this.mailService = ms;
        Session session = this.mailService.NewSession(props);
        Store store = this.mailService.Connect(session, password, host, user);
        // save store
        this.store = store;
        this.mailService.OpenFolder(this.store); // open here

        // load mails first time
        ObservableList<Mail> mails = LoadMoreMails();
        this.updateMailsList(mails);
        // set loaded mails to the list


        // Mails already read, close folder
        this.mailService.CloseFolder();
    }

    @FXML
    void initialize() {
        button_settings.setOnAction(event -> {

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            try {
                Main.SwitchScene(stage, "settings.fxml", true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        this.input_search_mail.setOnAction(event -> {
            System.out.println(((TextField)event.getSource()).getText());
            // filter mail here. change only this.sortedMails. To discard all filters, just make this.sortedMails = this.mails. Разберешься там
            // inbox.search()
            // In future will try to search in the local first, if not found, then on server
        });

        this.mail_list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Mail item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    try {
                        // add "update" button as a last element
                        int index = getIndex();
                        int lastIndex = mail_list.getItems().size() - 1;

                        if (index == lastIndex) {
                            // load more mails button
                            setGraphic(buttonLoadMoreMails);
                        } else {
                            // default cell with mail
                            FXMLLoader loader = new FXMLLoader(Main.class.getResource("mail_item.fxml"));
                            Parent cellRoot = loader.load();
                            MailItemController controller = loader.getController();
                            controller.setData(item);
                            setGraphic(cellRoot);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        buttonLoadMoreMails.setOnAction(e -> {
            try {
                this.mailService.OpenFolder(this.store);
            } catch (MessagingException ex) {
                System.err.println(ex.getMessage());
            }

            ObservableList<Mail> mailsToUpdate = null;

            try {
                mailsToUpdate = LoadMoreMails();
            } catch (MessagingException | IOException ex) {
                System.err.println(ex.getMessage());
            } finally {
                this.mailService.CloseFolder();
            }

            this.updateMailsList(mailsToUpdate);
        });
    }
}
