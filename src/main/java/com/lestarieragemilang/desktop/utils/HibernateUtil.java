package com.lestarieragemilang.desktop.utils;

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

    public static void shutdown() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            try {
                logger.info("Menutup koneksi pool HikariCP");
                hikariDataSource.close();
            } catch (Exception e) {
                logger.error("Terjadi kesalahan saat menutup koneksi pool HikariCP", e);
            }
        }

        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                logger.info("Menutup Hibernate SessionFactory");
                sessionFactory.close();
            } catch (Exception e) {
                logger.error("Terjadi kesalahan saat menutup Hibernate", e);
            }
        }
    }
}