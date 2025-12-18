package org.mailbird.Core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mailbird.Core.domain.model.User;

import java.util.List;

@Entity
@Table(name = "users", schema = "public")
public class UserEntity {
    public UserEntity() {

    }

    public UserEntity(User user) {
        // TODO: map User model to UserEntity
    }

    // Getters & setters
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false, length = 128, unique = true)
    private String email;

    @Setter
    @Getter
    @Column(nullable = false, length = 128)
    private String password;

    @Setter
    @Getter
    @Column(length = 128)
    private String host;

    @Getter
    @Setter
    @Column(length = 16)
    private String port;

//    // Relationships
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<FolderEntity> folders;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TagEntity> tags;
}
