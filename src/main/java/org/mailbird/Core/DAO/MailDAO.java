package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mailbird.Core.domain.entity.FolderEntity;
import org.mailbird.Core.domain.entity.MailEntity;
import org.mailbird.Core.domain.entity.UserEntity;
import org.mailbird.Core.domain.model.Mail;

import java.util.List;

public class MailDAO {
    private final SessionFactory sessionFactory;

    public MailDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public MailEntity GetById(long id, UserEntity owner) {
        try (Session session = this.sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT m FROM MailEntity m LEFT JOIN FETCH m.folders f " +
                                    "WHERE m.owner = :owner AND m.mail_id = :mailId",
                            MailEntity.class
                    )
                    .setParameter("owner", owner)
                    .setParameter("mailId", id)
                    .uniqueResult();
        }
    }

    public void SaveMails(List<MailEntity> mails, UserEntity currentUser, FolderEntity folder) {
        Transaction tx = null;
        try (Session session = this.sessionFactory.openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < mails.size(); i++) {
                mails.get(i).setOwner(currentUser);
                mails.get(i).getFolders().add(folder);
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

    public List<Mail> GetMailsByOwnerAndFolder(UserEntity owner, FolderEntity folder) {
        try (Session session = sessionFactory.openSession()) {
            List<MailEntity> mailEntities = session.createQuery(
                            "SELECT m FROM MailEntity m JOIN m.folders f "
                                    + "WHERE m.owner = :owner AND f = :folder ORDER BY m.mail_id DESC",
                            MailEntity.class
                    )
                    .setParameter("owner", owner)
                    .setParameter("folder", folder)
                    .list();

            return mailEntities.stream()
                    .map(this::entityToModel)
                    .toList();
        }
    }

    public void MoveToFolder(Long id, UserEntity user, FolderEntity folder) {
        try (Session session = this.sessionFactory.openSession()) {
            session.beginTransaction();

            MailEntity mail = this.GetById(id, user);
            if (mail != null) {
                if (mail.getFolders().contains(folder)) {
                    System.out.println("Mail with id " + id + " is already in the folder.");
                    session.getTransaction().commit();
                    return; // already in the folder
                }
                mail.getFolders().add(folder);
                session.merge(mail);
            } else {
                System.out.println("Mail with id " + id + " not found.");
            }

            session.getTransaction().commit();
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
