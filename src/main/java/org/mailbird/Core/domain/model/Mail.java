package org.mailbird.Core.domain.model;

import org.mailbird.Core.domain.entity.MailEntity;

import java.util.Date;

public record Mail(int id, String from, String to, Date date, String subject, String body, Boolean isRead, Boolean isDraft) {
    // TODO: now 'to' field is always 'me', need to parse actual recipients from message
//    public Mail(Message message, User me) throws MessagingException, IOException {
//        this(
//                message.getMessageNumber(),
//                ((InternetAddress) message.getFrom()[0]).getAddress(),
//                me.email(),
//                message.getReceivedDate(),
//                message.getSubject(),
//                "message.getContent()",
//                message.isSet(Flags.Flag.SEEN),
//                false
//        );
//    }

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

    // todo: сохранять в бд не весь объект body, а только текстовую часть
//    public Mail(MailEntity m) {
//        this(
//          m.getId(),
//          new  User(m.getFromUser()),
//          new  User(m.getToUser()),
//            m.getReceivedAt(),
//            m.getSubject(),
//            m.getText(),
//        );
//    }
}
