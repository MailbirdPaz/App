package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders", schema = "public")
public class FolderEntity {
    public FolderEntity() {}

    public FolderEntity(String title, UserEntity user) {
        this.title = title;
        this.user = user;
    }

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false, length = 64)
    private String title;

    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Getter
    @Setter
    @ManyToMany(mappedBy = "folders")
    private List<MailEntity> mails = new ArrayList<>();
}
