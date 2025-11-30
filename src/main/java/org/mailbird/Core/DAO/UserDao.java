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
        Session s = this.sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        s.persist(user);

        tx.commit();

        s.close();
    }

    public List<UserEntity> GetList() {
        Session s = this.sessionFactory.openSession();

        List<UserEntity> list = s.createQuery("from UserEntity ", UserEntity.class).list();

        s.close();

        return list;
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
