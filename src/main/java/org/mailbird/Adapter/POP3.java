package org.mailbird.Adapter;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import org.mailbird.Core.Port.IMail;

import java.util.Properties;

// Using POP3 adapter assumes using SMTP.
public class POP3 {
    public POP3() {

    }

//    @Override
    public Session NewSession(Properties props) throws NoSuchProviderException {
        Session session = Session.getInstance(props);
        session.setDebug(true);
        return session;
    }

    public Store Connect(Session session, String password, String host, String user) throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect(host, user, password);
        System.out.println("✅ Connected to " + "imaps" + " at " + host);
        return store;
    }

    //    @Override
    public Message[] LoadMails(Store store, int limit) throws MessagingException {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages;

        Boolean onlyUnread = false;
        if (onlyUnread) {
            // Только непрочитанные письма
            messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        } else {
            // Все письма
            int total = inbox.getMessageCount();
            int start = Math.max(1, total - limit + 1); // индекс первого из последних N
            int end = total;
            messages = inbox.getMessages(start, end);
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


//    @Override
    public void SendMail() {

    }

//    @Override
    public void LoadMail(int id) {

    }
}
