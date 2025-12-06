package org.mailbird.Core.domain.model;

import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import org.mailbird.Core.domain.entity.MailEntity;

import java.io.IOException;
import java.util.Date;

public record Mail(int id, User from, User to, Date date, String subject, String body, Boolean isRead, Boolean isDraft) {
    // TODO: now 'to' field is always 'me', need to parse actual recipients from message
    public Mail(Message message, User me) throws MessagingException, IOException {
        this(
                message.getMessageNumber(),
                new User(0L,
                        ((InternetAddress) message.getFrom()[0]).getAddress(),
                        ((InternetAddress) message.getFrom()[0]).getPersonal(),
                        "Surname",
                        "",
                        "",
                        ""
                ),
                me,
                message.getReceivedDate(),
                message.getSubject(),
                "message.getContent()",
                message.isSet(Flags.Flag.SEEN),
                false
        );
    }

    public Mail(MailEntity entity, User me) {
        this(
                entity.getMail_id(),
                new User((long)0, "", "", "", "", "", ""), // entity.getFromUser()
                me,
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
