package org.mailbird.Core.Controller;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;

import jakarta.mail.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.mailbird.Adapter.POP3;
import org.mailbird.Core.Services.AuthService;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Core.domain.model.User;
import org.mailbird.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    @FXML
    private ListView<Mail> mail_list;   

    @FXML
    private Label mail_title;

    @FXML
    private Button button_settings;

    @FXML
    private TextField input_search_mail;

    @FXML
    private WebView webviewMail;
    private WebEngine web;

    private final Button buttonLoadMoreMails = new Button("more...");
    private final ObservableList<Mail> displayedMails = FXCollections.observableArrayList();
    private POP3 mailService;
    private Store store;
    private final int flushLoadMax = 10; // load maximum 10 mails at once

    private AuthService authService;
    private Button buttonLoadMoreMails = new Button("more...");
    private ObservableList<Mail> displayedMails = FXCollections.observableArrayList();
    private POP3 mailService;
    private Store store;
    private int flushLoadMax = 10; // limit to load maximum 10 mails per loading

    // loads 10 mails and add to the list
    private ObservableList<Mail> LoadMoreMails() throws MessagingException, IOException {
        Message[] messages = this.mailService.LoadMails(this.store, this.displayedMails.size(), this.flushLoadMax);
        ObservableList<Mail> items = FXCollections.observableList(new ArrayList<Mail>());
        for (Message msg : messages) {
            items.addLast(new Mail(msg, this.authService.getUser()));
        }

        return items;
    }

    public void updateMailsList(ObservableList<Mail> mails) {
        if (mails == null) return;
        this.displayedMails.addAll(mails);
        mail_list.setItems(this.displayedMails);
    }

    public void connect(String password, String host, String email, AuthService s) throws MessagingException, IOException {
        this.authService = s;

        POP3 ms = new POP3();
        this.mailService = ms;
        Session session = this.mailService.NewSession(authService.getCredentials().AsProperties());
        Store store = this.mailService.Connect(session, authService.getCredentials());
        // save store
        this.store = store;
        this.mailService.OpenFolder(this.store);

        ObservableList<Mail> mails = LoadMoreMails();
        this.updateMailsList(mails);

        this.mailService.CloseFolder();
    }

    @FXML
    void initialize() {
        web = webviewMail.getEngine();
        web.setJavaScriptEnabled(false); // secure

        // click on the mail -> show content
        mail_list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showMail(newV);
        });
        mail_list.setOnMouseClicked(e -> {
            Mail m = mail_list.getSelectionModel().getSelectedItem();
            if (m != null) showMail(m);
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
            try {
                this.mailService.OpenFolder(this.store);
                ObservableList<Mail> mailsToUpdate = LoadMoreMails();
                this.updateMailsList(mailsToUpdate);
            } catch (MessagingException | IOException ex) {
                System.err.println(ex.getMessage());
            } finally {
                this.mailService.CloseFolder();
            }
        });
    }

    // mail content render
    private void showMail(Mail mail) {
        // show theme
        mail_title.setText(mail.subject() == null ? "(без темы)" : mail.subject());

        try {
            // try to render body
            try {
                Object body = mail.body();
                if (body instanceof String || body instanceof Multipart) {
                    String html = renderBody(body);
                    web.loadContent(html, "text/html");
                    return;
                }
            } catch (jakarta.mail.FolderClosedException fcx) {
                // упадём в «пере-рендер из Store» ниже
            } catch (Exception ignore) {
                // если локальный рендер не удался – перефетчим из Store
            }

            // Гарантированный путь: открыть INBOX, взять Message и отрендерить ПРЯМО СЕЙЧАС
            String html = fetchAndRenderHtml(mail.id());
            if (html == null) {
                html = "<!doctype html><html><body><i>(Empty mail)</i></body></html>";
            }
            web.loadContent(html, "text/html");

        } catch (Exception e) {
            e.printStackTrace();
            String esc = escape(e.getMessage() == null ? e.toString() : e.getMessage());
            web.loadContent("<!doctype html><html><body><pre>" + esc + "</pre></body></html>", "text/html");
        }
    }

    private String fetchAndRenderHtml(int messageNumber) {
        try {
            // Открываем INBOX только на время чтения
            this.mailService.OpenFolder(this.store);
            var inbox = this.store.getFolder("INBOX");
            if (!inbox.isOpen()) inbox.open(jakarta.mail.Folder.READ_ONLY);

            jakarta.mail.Message msg = inbox.getMessage(messageNumber);
            Object content = msg.getContent();          // читаем тело
            // ВАЖНО: рендерим ПРЯМО сейчас, пока папка открыта
            return renderBody(content);

        } catch (Exception ex) {
            System.err.println("fetchAndRenderHtml failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return null;
        } finally {
            this.mailService.CloseFolder();
        }
    }


    private Object fetchBodyFromStore(int messageNumber) {
        try {
            // Открываем INBOX только на чтение, достаём нужное письмо по номеру
            this.mailService.OpenFolder(this.store);
            var inbox = this.store.getFolder("INBOX");
            if (!inbox.isOpen()) inbox.open(jakarta.mail.Folder.READ_ONLY);

            Message msg = inbox.getMessage(messageNumber);
            Object content = msg.getContent();   // вот тут реально читается тело

            return content;
        } catch (Exception ex) {
            System.err.println("fetchBodyFromStore failed: " + ex.getMessage());
            return null;
        } finally {
            this.mailService.CloseFolder();
        }
    }



    private String renderBody(Object body) throws Exception {
        if (body == null) return "<!doctype html><html><body><i>(Empty mail)</i></body></html>";

        if (body instanceof String s) {
            String t = s.trim();
            boolean looksHtml = t.startsWith("<!DOCTYPE") || t.startsWith("<html") || t.contains("<body");
            return looksHtml ? s : wrapHtml("<pre>" + escape(s) + "</pre>");
        }

        if (body instanceof Multipart mp) return renderMultipartHtml(mp);

        return wrapHtml("<pre>" + escape(String.valueOf(body)) + "</pre>");
    }

    private static final Pattern CID_SRC =
            Pattern.compile("(?i)src\\s*=\\s*\"cid:([^\"]+)\"");

    private String renderMultipartHtml(Multipart root) throws Exception {
        // Кандидаты на отображение
        String htmlCandidate = null;
        String plainCandidate = null;
        String htmlContentLocation = null;
        Map<String, InlineImage> cidImages = new HashMap<>();

        Deque<Multipart> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Multipart mp = stack.pop();

            // Узнаем подтип multiparta (нужно для alternative)
            String mpType = (mp.getContentType() == null) ? "" : mp.getContentType().toLowerCase(Locale.ROOT);
            boolean isAlternative = mpType.startsWith("multipart/alternative");

            // RFC советует для multipart/alternative идти с конца (последняя часть — самая «богатая»)
            int from = isAlternative ? mp.getCount() - 1 : 0;
            int to   = isAlternative ? -1 : mp.getCount();
            int step = isAlternative ? -1 : 1;

            for (int i = from; i != to; i += step) {
                BodyPart part = mp.getBodyPart(i);
                Object content = part.getContent();
                String ctype = safeContentType(part);

                // Вложенный multipart — раскладываем дальше
                if (content instanceof Multipart nested) {
                    stack.push(nested);
                    continue;
                }

                if (isHtml(ctype)) {
                    // Всегда предпочитаем HTML — перезаписываем кандидата
                    htmlCandidate = content.toString();
                    htmlContentLocation = headerFirst(part, "Content-Location");
                } else if (isPlain(ctype)) {
                    // plain берём как запасной вариант (первый встреченный)
                    if (plainCandidate == null) {
                        plainCandidate = content.toString();
                    }
                } else if (ctype.startsWith("image/")) {
                    String cid = contentId(part);
                    if (cid != null) {
                        cidImages.put(cid, toInlineImage(part, ctype));
                    }
                }
            }
        }

        String html;
        if (htmlCandidate != null) {
            html = htmlCandidate;
            // подставим cid-картинки и base href
            html = replaceCid(html, cidImages);
            if (htmlContentLocation != null && !htmlContentLocation.isBlank()) {
                html = injectBase(html, htmlContentLocation);
            }
            return ensureHtmlShellIfNeeded(html);
        }

        if (plainCandidate != null) {
            return wrapHtml("<pre>" + escape(plainCandidate) + "</pre>");
        }

        return "<i>(Нет поддерживаемого содержимого)</i>";
    }


    private String safeContentType(Part part) throws Exception {
        String ct = part.getContentType();
        return (ct == null) ? "application/octet-stream" : ct.toLowerCase(Locale.ROOT);
    }
    private boolean isHtml(String ctype) {
        return ctype.startsWith("text/html");
    }
    private boolean isPlain(String ctype) {
        return ctype.startsWith("text/plain");
    }

    private record InlineImage(String mime, String base64) {}

    private InlineImage toInlineImage(BodyPart part, String ctype) throws Exception {
        try (InputStream is = part.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String b64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String mime = ctype.split(";")[0].trim();
            return new InlineImage(mime, b64);
        }
    }

    private String contentId(Part part) throws Exception {
        String id = headerFirst(part, "Content-ID");
        if (id == null) return null;
        id = id.trim();
        if (id.startsWith("<") && id.endsWith(">")) id = id.substring(1, id.length() - 1);
        return id;
    }
    private String headerFirst(Part part, String name) throws Exception {
        String[] vals = part.getHeader(name);
        return (vals != null && vals.length > 0) ? vals[0] : null;
    }

    private String replaceCid(String html, Map<String, InlineImage> cidImages) {
        if (cidImages.isEmpty()) return html;
        Matcher m = CID_SRC.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String cid = m.group(1);
            InlineImage img = cidImages.get(cid);
            if (img != null) {
                String dataUrl = "src=\"data:" + img.mime() + ";base64," + img.base64() + "\"";
                m.appendReplacement(sb, Matcher.quoteReplacement(dataUrl));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String injectBase(String html, String href) {
        int headOpen = indexOfIgnoreCase(html, "<head");
        if (headOpen >= 0) {
            int headClose = indexOfIgnoreCase(html, ">", headOpen);
            if (headClose > headOpen) {
                return html.substring(0, headClose + 1)
                        + "<base href=\"" + escapeAttr(href) + "\">"
                        + html.substring(headClose + 1);
            }
        }
        return """
                <!doctype html><html><head><meta charset="UTF-8"><base href="%s"></head><body>%s</body></html>
               """.formatted(escapeAttr(href), html);
    }

    private int indexOfIgnoreCase(String s, String what) {
        return indexOfIgnoreCase(s, what, 0);
    }

    private int indexOfIgnoreCase(String s, String what, int from) {
        return s.toLowerCase(Locale.ROOT).indexOf(what.toLowerCase(Locale.ROOT), from);
    }

    private String ensureHtmlShellIfNeeded(String html) {
        String t = html.trim();
        boolean looksHtml = t.startsWith("<!DOCTYPE") || t.startsWith("<html") || t.contains("<body");
        return looksHtml ? html : wrapHtml(html);
    }

    private String wrapHtml(String inner) {
        return "<!doctype html><html><head><meta charset=\"UTF-8\"></head><body>" + inner + "</body></html>";
    }

    private String escape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private String escapeAttr(String s) {
        return escape(s).replace("\"","&quot;").replace("'","&#39;");
    }
}
