package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.model.Mail;

import java.util.List;

public class MailDAO {
    private final SessionFactory sessionFactory;

    public MailDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void SaveMails(List<MailEntity> mails) {
        Transaction tx = null;
        try (Session session = this.sessionFactory.openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < mails.size(); i++) {
                session.persist(mails.get(i));
                System.out.println("Persist #" + i);

                if ((i + 1) % 20 == 0) { // flush every 20 emails, to avoid memory problem
                    session.flush();
                    session.clear();
                }
            }

            tx.commit();
            System.out.println("tx.commit() done");
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    private Mail entityToModel(MailEntity m) {
        return new Mail(m);
    }

    public List<Mail> GetMails() {
        try (Session session = this.sessionFactory.openSession()) {
            List<MailEntity> mailEntities = session.createQuery("FROM MailEntity", MailEntity.class).list();
            return mailEntities.stream().map(this::entityToModel).toList();
        }
    }
}
