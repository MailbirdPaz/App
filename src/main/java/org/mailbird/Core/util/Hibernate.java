package org.mailbird.Core.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Hibernate {
    private SessionFactory sessionFactory;

    /// Hibernate constructor sets up the connection to the database
    public Hibernate(String url) {
        if (this.sessionFactory == null) {
            try {
                Configuration cfg = new Configuration().configure("org/mailbird/hibernate.cfg.xml");
                cfg.setProperty("hibernate.connection.url", url);
                sessionFactory = cfg.buildSessionFactory();
                System.out.println("✅ Hibernate connected successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("❌ Failed to initialize Hibernate", ex);
            }
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
