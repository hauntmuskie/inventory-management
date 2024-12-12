package com.lestarieragemilang.desktop.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lestarieragemilang.desktop.App;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    private static boolean databaseAvailable = false;

    static {
        try {
            sessionFactory = new Configuration().configure(
                App.class.getResource("hibernate.cfg.xml")
            ).buildSessionFactory();
            databaseAvailable = true;
        } catch (Exception e) {
            logger.warn("Database connection failed. Application will run in limited mode.", e);
            sessionFactory = null;
            databaseAvailable = false;
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static boolean isDatabaseAvailable() {
        return databaseAvailable;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}