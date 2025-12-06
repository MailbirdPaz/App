package org.mailbird.Core.domain.entity;

import jakarta.mail.*;
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
        this.fromUser = new UserEntity(m.from()); // TODO: 'UserEntity' does not parse m.from, i need to implement this constructor
        this.toUser = new UserEntity(m.to());
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
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "received_at")
    private Date receivedAt;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "is_starred")
    private boolean isStarred;

    @Column(name = "is_draft")
    private boolean isDraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id")
    private UserEntity fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id")
    private UserEntity toUser;

    @ManyToMany(mappedBy = "mails")
    private List<FolderEntity> folders;

    @ManyToMany(mappedBy = "mails")
    private List<TagEntity> tags;

    // TODO: set @Getter / @Setter annotations and delete those methods
    // Getters & setters
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getText() { return text; }
    public Date getReceivedAt() { return receivedAt; }
    public boolean isRead() { return isRead; }
    public boolean isStarred() { return isStarred; }
    public boolean isDraft() { return isDraft; }
    public UserEntity getFromUser() { return fromUser; }
    public UserEntity getToUser() { return toUser; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setText(String text) { this.text = text; }
    public void setReceivedAt(Date receivedAt) { this.receivedAt = receivedAt; }
    public void setRead(boolean read) { isRead = read; }
    public void setStarred(boolean starred) { isStarred = starred; }
    public void setDraft(boolean draft) { isDraft = draft; }
    public void setFromUser(UserEntity fromUser) { this.fromUser = fromUser; }
    public void setToUser(UserEntity toUser) { this.toUser = toUser; }
}
