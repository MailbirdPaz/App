package org.mailbird.Core.domain.model;

import org.mailbird.Core.domain.entity.MailEntity;

import java.util.Date;

public record Mail(long id, String from, String to, Date date, String subject, String body, Boolean isRead, Boolean isDraft) {
    public Mail(MailEntity entity) {
        this(
                entity.getMail_id(),
                entity.getFrom(),
                entity.getTo(),
                entity.getReceivedAt(),
                entity.getSubject(),
                entity.getText(),
                entity.isRead(),
                entity.isDraft()
        );
    }
}
