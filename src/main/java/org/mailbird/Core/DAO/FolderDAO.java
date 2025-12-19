package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mailbird.Core.domain.entity.FolderEntity;
import org.mailbird.Core.domain.entity.UserEntity;

import java.util.List;

public class FolderDAO {
    private final SessionFactory sessionFactory;

    public FolderDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void Create(String title, UserEntity owner) {
        Transaction tx = null;

        try (Session session = this.sessionFactory.openSession()) {
            tx = session.beginTransaction();

            FolderEntity folder = new FolderEntity(title, owner);
            session.persist(folder);

            tx.commit();
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    public List<FolderEntity> GetByOwner(UserEntity owner) {
        try (Session session = this.sessionFactory.openSession()) {
            return session.createQuery("FROM FolderEntity f WHERE f.user = :owner", FolderEntity.class)
                    .setParameter("owner", owner)
                    .list();
        }
    }

    public FolderEntity GetByOwnerAndTitle(UserEntity owner, String title) {
        try (Session session = this.sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM FolderEntity f WHERE f.title = :title AND f.user = :owner", FolderEntity.class)
                    .setParameter("title", title)
                    .setParameter("owner", owner)
                    .uniqueResult();
        }
    }
}
