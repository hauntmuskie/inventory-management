package com.lestarieragemilang.desktop.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.lestarieragemilang.desktop.App;

public abstract class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure(App.class.getResource("hibernate.cfg.xml")).buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}