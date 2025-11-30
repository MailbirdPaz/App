package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import org.mailbird.Core.domain.model.Mail;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "mails", schema = "public")
public class MailEntity {
    public MailEntity() {

    }

    public MailEntity(Mail m, boolean isDraft) {
        this.id = (long) m.id();
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
