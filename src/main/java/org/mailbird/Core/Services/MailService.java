package org.mailbird.Core.Services;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import org.mailbird.Core.DAO.MailDAO;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Core.util.Credentials;

import java.util.List;
import java.util.Properties;

public class MailService {
    Folder folder;
    Session session;
    Store store;
    MailDAO mailDAO;

    public MailService(MailDAO mailDAO) {
        this.mailDAO = mailDAO;
    }

    public void Connect(Credentials c) throws MessagingException {
        Session session = Session.getInstance(c.AsProperties());
        session.setDebug(false);
        this.session = session;

        Store store = session.getStore("imaps"); // by default
        store.connect(c.host(), c.user(), c.password());
        System.out.println("✅ Connected to " + "imaps" + " at " + c.host());
        this.store = store;
    }

    public void Disconnect() throws MessagingException {
        this.session = null;
        if (this.store != null && store.isConnected()) {
            this.store.close();
        }
    }

    //    @Override
    public void OpenFolder() throws MessagingException {
        Folder inbox = this.store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        this.folder = inbox;
    }

    public Message[] LoadMails(int offset, int limit) throws MessagingException {
        Message[] messages;

        Boolean onlyUnread = false;
        if (onlyUnread) {
            // Только непрочитанные письма
            messages = this.folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        } else {
            // Все письма
            int total = this.folder.getMessageCount();
            int end = total - offset;
            int start = Math.max(1, end - limit + 1);
            messages = this.folder.getMessages(start, end);
        }

        // Сортируем так, чтобы newest first
        // Для IMAP порядок getMessages может быть oldest first
        if (messages.length > 1) {
            Message[] reversed = new Message[messages.length];
            for (int i = 0; i < messages.length; i++) {
                reversed[i] = messages[messages.length - 1 - i];
            }
            messages = reversed;
        }

        // НЕ закрываем inbox здесь, чтобы можно было читать письма в вызывающем коде
        // inbox.close(false);

        return messages;
    }

    public void CloseFolder() {
        if (folder != null) {
            try {
                folder.close(false);
            } catch (MessagingException ex) {}
        }
    }

    public void SaveToDatabase(Mail[] messages) {
        this.mailDAO.SaveMails(messages);
    }

    public List<MailEntity> ListFromDatabase(MailEntity[] mails) {
        return this.mailDAO.GetMails();
    }


    //    @Override
    public void SendMail() {

    }

    //    @Override
    public void LoadMail(int id) {

    }
}
