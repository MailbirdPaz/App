package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.entity.UserEntity;
import org.mailbird.Core.domain.model.Mail;

import java.util.List;

public class MailDAO {
    private final SessionFactory sessionFactory;

    public MailDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void SaveMails(List<MailEntity> mails, UserEntity currentUser) {
        Transaction tx = null;
        try (Session session = this.sessionFactory.openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < mails.size(); i++) {
                mails.get(i).setOwner(currentUser);
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

    public List<Mail> GetMailsByOwner(UserEntity owner) {
        try (Session session = this.sessionFactory.openSession()) {
            List<MailEntity> mailEntities = session.createQuery(
                    "FROM MailEntity m WHERE m.owner.id = :ownerId ORDER BY m.mail_id DESC", MailEntity.class
                )
                    .setParameter("ownerId", owner.getId())
                    .list();

            return mailEntities.stream().map(this::entityToModel).toList();
        }
    }

    public void DeleteMail(long mailUid) {
        try (Session session = this.sessionFactory.openSession()) {
            session.beginTransaction();

            MailEntity toRemove = session.find(MailEntity.class, mailUid);
            if (toRemove != null) {
                session.remove(toRemove);
            }

            session.getTransaction().commit();
        }
    }
}
