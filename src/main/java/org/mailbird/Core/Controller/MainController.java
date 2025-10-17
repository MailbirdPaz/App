package org.mailbird.Core.Controller;


import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.mailbird.Adapter.POP3;
import org.mailbird.Core.Entity.Mail;
import org.mailbird.Main;

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
    void onSearchTextChange(ActionEvent event) {

    }

    private POP3 mailService;

    public void connect(String user, String password, String host) throws MessagingException {
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

        Message[] messages = this.mailService.LoadMails(store, 5);

        for (Message msg : messages) {
            String from = ((InternetAddress) msg.getFrom()[0]).getAddress();
            System.out.printf("[%s] %s%n", from, msg.getSubject());
        }
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
    }
}
