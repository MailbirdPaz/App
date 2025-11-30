package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.model.Mail;

import java.util.List;

public class MailDAO {
    private final SessionFactory sessionFactory;

    public MailDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void SaveMails(Mail[] jakartaMail) {
        try (Session session = this.sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            for (Mail mail : jakartaMail) {
                MailEntity mailEntity = new MailEntity(mail, false);
                session.persist(mailEntity);
            }


            transaction.commit();
        }
    }

    private Mail entityToModel(MailEntity m) {
        return new Mail(
                m.getId(),
                m.getFromAddress(),
                m.getToAddress(),
                m.getSubject(),
                m.getBody(),
                m.getReceivedDate()
        );
    }

    public List<Mail> GetMails() {
        try (Session session = this.sessionFactory.openSession()) {
            List<MailEntity> mailEntities = session.createQuery("FROM MailEntity", MailEntity.class).list();
            return mailEntities.stream().map(this::entityToModel).toList();
        }
    }
}
