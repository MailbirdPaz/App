package org.mailbird.Core.Services;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.mailbird.Core.DAO.MailDAO;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.model.Mail;
import org.mailbird.Core.util.Credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailService {
    IMAPFolder folder;
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
        IMAPFolder inbox = (IMAPFolder) this.store.getFolder("INBOX"); // "INBOX" /[Gmail]/Вся почта
        inbox.open(Folder.READ_ONLY);
        this.folder = inbox;
    }

    public Message[] LoadMails(int limit, long lastUid) throws MessagingException {
        Message[] messages;

        if (lastUid == 0) {
            long total = this.folder.getUIDNext() - 1;;
            long start = Math.max(1, total - 9);
            long end = total;
            messages = this.folder.getMessagesByUID(start, end);
        } else {
            long lastUidOnServer = this.folder.getUIDNext() - 1;
            if (lastUid >= lastUidOnServer) {
                messages = new Message[0];
            } else {
                messages = this.folder.getMessagesByUID(lastUid + 1, UIDFolder.LASTUID);
            }
        }

        // do not close folder here, so we can load info from mail later
        // sort from the newest to the oldest (because server order can be different)
        return sortMessages(messages);
    }

    // load 10 old mails from the oldest UID
    public Message[] LoadOldMails(int flush, long oldestUid) throws MessagingException {
        long end = oldestUid - 1;
        long start = oldestUid - flush + 1;
        Message[] messages = this.folder.getMessagesByUID(start, end);

        return sortMessages(messages);
    }

    private Message[] sortMessages(Message[] messages) {
        Message[] reversed = new Message[messages.length];
        for (int i = 0; i < messages.length; i++) {
            reversed[i] = messages[messages.length - 1 - i];
        }
        return reversed;
    }

    public void CloseFolder() {
        if (folder != null) {
            try {
                folder.close(false);
            } catch (MessagingException ex) {}
        }
    }

    ///  Saves provided Message List to the database and cast Message to the MailEntity.
    public List<MailEntity> SaveToDatabase(Message[] messages) {
        // cast messages to the MailEntity here
        List<MailEntity> mails = this.messagesToMailsEntity(messages);
        // save to the database
        this.mailDAO.SaveMails(mails);

        return mails;
    }

    public List<MailEntity> messagesToMailsEntity(Message[] messages) {
        ArrayList<MailEntity> mails = new ArrayList<>();

        for (Message message : messages) {
            try {
                long uid = folder.getUID(message);
                mails.add(new MailEntity(message, uid));
            } catch (Exception ex) {
                Logger.getLogger(MailEntity.class.getName()).log(Level.SEVERE, "failed to cast [Message] -> [MailEntity]", ex);
            }
        }

        return mails;
    }

    public List<Mail> ListFromDatabase() {
        return this.mailDAO.GetMails();
    }

    public void SendMail(Credentials cred, String to, String subject, String body) throws MessagingException {

        // SMTP configuration (correct)
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");          // FIX 1
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session smtpSession = Session.getInstance(props);
        smtpSession.setDebug(false);

        MimeMessage message = new MimeMessage(smtpSession);
        message.setFrom(new InternetAddress(cred.user()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        // add !DOCTYPE because HTMLEditor does not add it
        message.setContent("<!DOCTYPE html>" + body, "text/html; charset=UTF-8");



        Transport transport = smtpSession.getTransport("smtp");

        transport.connect(
                "smtp.gmail.com",     // Gmail SMTP server
                cred.user(),          // email address / username
                cred.password()       // app password
        );

        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    ///  deletes mail with provided UID from the server and database
    public void DeleteMail(long uid) throws MessagingException {
        this.folder.open(Folder.READ_WRITE);
        Message message = this.folder.getMessageByUID(uid);
        if (message != null) {
            message.setFlag(Flags.Flag.DELETED, true);

            // delete from database
            mailDAO.DeleteMail(uid);

            // TODO: show notification about successful deletion instead of log
            System.out.println("Mail with UID " + uid + " marked for deletion.");
        } else {
            System.out.println("Mail with UID " + uid + " not found.");
        }

        this.folder.close(true); // pass true to expunge deleted messages
    }
}
