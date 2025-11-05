package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users", schema = "public")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 64)
    private String surname;

    @Column(nullable = false, length = 128, unique = true)
    private String email;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(nullable = false, length = 16)
    private String protocol;

    @Column(length = 128)
    private String host;

    @Column(length = 16)
    private String port;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEntity> folders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagEntity> tags;

    @OneToMany(mappedBy = "fromUser")
    private List<MailEntity> sentMails;

    @OneToMany(mappedBy = "toUser")
    private List<MailEntity> receivedMails;

    // Getters & setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProtocol() { return protocol; }
    public String getHost() { return host; }
    public String getPort() { return port; }

    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setHost(String host) { this.host = host; }
    public void setPort(String port) { this.port = port; }
}
