package org.mailbird.Core.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mailbird.Core.domain.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public class UserDao {
    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void Insert(UserEntity user) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<UserEntity> GetList() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from UserEntity ", UserEntity.class).list();
        }
    }

    public Optional<UserEntity> GetByEmail(String email) {
        try (Session session = this.sessionFactory.openSession()) {
            System.out.println("Try to get user by email: " + email);
            return session.createQuery("FROM UserEntity WHERE email = :email", UserEntity.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        }
    }
}
