package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "folders", schema = "public")
public class FolderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String title;

    @Column(nullable = false, length = 16)
    private String color;

    @Column(name = "icon_path", length = 255)
    private String iconPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToMany
    @JoinTable(
            name = "mail_folders",
            joinColumns = @JoinColumn(name = "folder_id"),
            inverseJoinColumns = @JoinColumn(name = "mail_id")
    )
    private List<MailEntity> mails;

    // Getters & setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getColor() { return color; }
    public String getIconPath() { return iconPath; }
    public UserEntity getUser() { return user; }
    public List<MailEntity> getMails() { return mails; }

    public void setTitle(String title) { this.title = title; }
    public void setColor(String color) { this.color = color; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    public void setUser(UserEntity user) { this.user = user; }
    public void setMails(List<MailEntity> mails) { this.mails = mails; }

}
