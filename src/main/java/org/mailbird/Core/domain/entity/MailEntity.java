package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "mails", schema = "public")
public class MailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

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

    // Getters & setters
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getText() { return text; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public boolean isRead() { return isRead; }
    public boolean isStarred() { return isStarred; }
    public boolean isDraft() { return isDraft; }
    public UserEntity getFromUser() { return fromUser; }
    public UserEntity getToUser() { return toUser; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setText(String text) { this.text = text; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public void setRead(boolean read) { isRead = read; }
    public void setStarred(boolean starred) { isStarred = starred; }
    public void setDraft(boolean draft) { isDraft = draft; }
    public void setFromUser(UserEntity fromUser) { this.fromUser = fromUser; }
    public void setToUser(UserEntity toUser) { this.toUser = toUser; }
}
