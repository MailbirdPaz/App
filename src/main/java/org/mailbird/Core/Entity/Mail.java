package org.mailbird.Core.Entity;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import java.io.IOException;
import java.util.Date;

public record Mail(int id, User from, User to, Date date, String subject, Object body) {
    public Mail(Message message) throws MessagingException, IOException {
        this(
                message.getMessageNumber(),
                new User(0,
                        ((InternetAddress) message.getFrom()[0]).getAddress(),
                        ((InternetAddress) message.getFrom()[0]).getPersonal()
                ),
                new User(1, "me", "me"),
                message.getSentDate(),
                message.getSubject(),
                message.getContent()
        );
    }
}
