package org.mailbird.Core.domain.model;

import lombok.With;
import org.mailbird.Core.domain.entity.UserEntity;

@With
public record User(Long id, String email, String name, String surname, String protocol, String host, String port) {
    public User(UserEntity entity) {
            this(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getSurname(),
                entity.getProtocol(),
                entity.getHost(),
                entity.getPort()
            );
    }
}

//  String passwd, String host, String port
