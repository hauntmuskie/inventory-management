package com.lestarieragemilang.desktop.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lestarieragemilang.desktop.utils.HibernateUtil;

import java.util.List;

public class GenericDao<T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericDao.class);
    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public void save(T entity) {
        executeInsideTransaction(session -> session.persist(entity));
    }

    public void update(T entity) {
        executeInsideTransaction(session -> session.merge(entity));
    }

    public void checkDeleteConstraints(T entity) throws ConstraintViolationException {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.remove(entity);
                session.flush();
                transaction.rollback();
            } catch (ConstraintViolationException e) {
                transaction.rollback();
                throw e;
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error checking delete constraints", e);
                throw new RuntimeException("Error checking delete constraints", e);
            }
        }
    }

    public void delete(T entity) throws ConstraintViolationException {
        executeInsideTransaction(session -> session.remove(entity));
    }

    public T findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(entityClass, id);
        } catch (Exception e) {
            logger.error("Error finding entity by id", e);
            throw new RuntimeException("Error finding entity by id", e);
        }
    }

    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
            Root<T> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);
            return session.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            logger.error("Error finding all entities", e);
            throw new RuntimeException("Error finding all entities", e);
        }
    }

    public String generateId(String prefix, int bound) {
        try (Session session = sessionFactory.openSession()) {
            String generatedId;
            boolean exists;
            do {
                generatedId = prefix + (int) (Math.random() * bound);

                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
                Root<T> root = criteriaQuery.from(entityClass);

                criteriaQuery.select(cb.literal(1L))
                        .where(cb.equal(root.get("id"), generatedId));

                exists = session.createQuery(criteriaQuery).uniqueResult() != null;
            } while (exists);
            return generatedId;
        } catch (Exception e) {
            logger.error("Error generating ID", e);
            throw new RuntimeException("Error generating ID", e);
        }
    }

    private void executeInsideTransaction(SessionAction action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            action.execute(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new RuntimeException("Transaction failed", e);
        }
    }

    @FunctionalInterface
    private interface SessionAction {
        void execute(Session session);
    }
}
