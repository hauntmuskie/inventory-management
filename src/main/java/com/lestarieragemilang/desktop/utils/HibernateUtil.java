package com.lestarieragemilang.desktop.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lestarieragemilang.desktop.App;
import com.zaxxer.hikari.HikariDataSource;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    private static boolean databaseAvailable = false;
    private static HikariDataSource hikariDataSource;

    static {
        try {
            Configuration configuration = new Configuration().configure(
                    App.class.getResource("hibernate.cfg.xml"));
            sessionFactory = configuration.buildSessionFactory();
            databaseAvailable = true;

            ServiceRegistry serviceRegistry = ((org.hibernate.internal.SessionFactoryImpl) sessionFactory)
                    .getServiceRegistry();
            hikariDataSource = serviceRegistry
                    .getService(ConnectionProvider.class)
                    .unwrap(HikariDataSource.class);
        } catch (Exception e) {
            logger.warn("Koneksi database gagal. Aplikasi akan berjalan dalam mode terbatas.", e);
            sessionFactory = null;
            hikariDataSource = null;
            databaseAvailable = false;
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static boolean isDatabaseAvailable() {
        return databaseAvailable;
    }

    public static void reinitialize() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            shutdown();
        }

        try {
            Configuration configuration = new Configuration().configure(
                    App.class.getResource("hibernate.cfg.xml"));
            sessionFactory = configuration.buildSessionFactory();
            databaseAvailable = true;

            ServiceRegistry serviceRegistry = ((org.hibernate.internal.SessionFactoryImpl) sessionFactory)
                    .getServiceRegistry();
            hikariDataSource = serviceRegistry
                    .getService(ConnectionProvider.class)
                    .unwrap(HikariDataSource.class);

            logger.info("Database connection reinitialized successfully");
        } catch (Exception e) {
            logger.error("Failed to reinitialize database connection", e);
            sessionFactory = null;
            hikariDataSource = null;
            databaseAvailable = false;
        }
    }

    public static synchronized void shutdown() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            try {
                logger.info("Closing HikariCP connection pool");
                hikariDataSource.close();
            } catch (Exception e) {
                logger.error("Error closing HikariCP connection pool", e);
            } finally {
                hikariDataSource = null;
            }
        }

        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                logger.info("Closing Hibernate SessionFactory");
                sessionFactory.close();
            } catch (Exception e) {
                logger.error("Error closing Hibernate SessionFactory", e);
            } finally {
                sessionFactory = null;
            }
        }
        databaseAvailable = false;
    }

    public static Session getCurrentSession() {
        Session session = null;
        try {
            session = getSessionFactory().getCurrentSession();
        } catch (Exception e) {
            session = getSessionFactory().openSession();
        }
        return session;
    }
}