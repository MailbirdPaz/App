package org.mailbird.Core.domain.model;

import lombok.With;
import org.mailbird.Core.domain.entity.UserEntity;

@With
public record User(Long id, String email, String password, String host, String port) {
    public User(UserEntity entity) {
            this(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getHost(),
                entity.getPort()
            );
    }
}

//  String passwd, String host, String port
