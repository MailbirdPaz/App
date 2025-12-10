package org.mailbird.Core.domain.entity;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mailbird.Core.domain.model.Mail;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Entity
@Table(name = "mails", schema = "public")
public class MailEntity {
    public MailEntity() {

    }

    /// Create MailEntity from provided Message. It also extracts only text content from Message and ignore images.
    /// Relations like from, folder, ..., i need to handle out of this constructor
    public MailEntity(Message message) throws Exception {
        this.mail_id = message.getMessageNumber();
        this.subject = message.getSubject();
        this.receivedAt = message.getReceivedDate();
        this.isRead = message.getFlags().contains(Flags.Flag.SEEN);
        this.isDraft = false;
        this.text = this.getTextFromMessage(message);
        // todo: make for multiple from and to | add personal names
        this.from = ((InternetAddress) message.getFrom()[0]).getAddress();
        this.to = ((InternetAddress) message.getRecipients(Message.RecipientType.TO)[0]).getAddress();
    }

    private String getBestPart(Multipart multipart) throws Exception {
        String plain = null;
        String html = null;

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            // пропускаем вложения
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                continue;
            }

            Object content = part.getContent();

            if (part.isMimeType("text/html")) {
                html = content.toString();
            }
            else if (part.isMimeType("text/plain")) {
                plain = content.toString();
            }
            else if (content instanceof Multipart) {
                String nested = getBestPart((Multipart) content);
                if (nested != null) {
                    return nested; // если вложенный HTML найден — отдаём сразу
                }
            }
        }

        // HTML важнее → если есть — возвращаем его
        if (html != null) return html;

        // иначе plain
        return plain;
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("multipart/*")) {
            return getBestPart((Multipart) message.getContent());
        }

        if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        }

        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }

        return "";
    }

    public MailEntity(Mail m, boolean isDraft) {
        this.mail_id = m.id();
        this.subject = m.subject();
        this.text = String.valueOf(m.body());
        this.receivedAt = m.date();
        this.isRead = m.isRead();
        this.from = m.from();
        this.to = m.to();
        this.isDraft = isDraft;

        // TODO: folders and tags
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(nullable = false, updatable = false)
    @Setter()
    @Getter()
    private int mail_id;

    @Column(nullable = false, length = 255)
    @Getter
    @Setter
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Getter
    @Setter
    private String text;

    @Column(name = "received_at")
    @Getter
    @Setter
    private Date receivedAt;

    @Column(name = "is_read")
    @Getter
    @Setter
    private boolean isRead;

    @Column(name = "is_starred")
    @Getter
    @Setter
    private boolean isStarred;

    @Column(name = "is_draft")
    @Getter
    @Setter
    private boolean isDraft;

    @Column(name = "from_email")
    @Getter
    @Setter
    private String from;

    @Column(name = "to_email")
    @Getter
    @Setter
    private String to;

    @ManyToMany(mappedBy = "mails")
    private List<FolderEntity> folders;

    @ManyToMany(mappedBy = "mails")
    private List<TagEntity> tags;
}
