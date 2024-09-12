package com.lestarieragemilang.desktop.repository;

import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericDao<T> {
    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;

    public GenericDao(Class<T> entityClass, SessionFactory sessionFactory) {
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }

    public void save(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
        }
    }

    public void update(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(entity);
            session.getTransaction().commit();
        }
    }

    public void delete(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(entity);
            session.getTransaction().commit();
        }
    }

    public T findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaQuery<T> criteriaQuery = session.getCriteriaBuilder().createQuery(entityClass);
            criteriaQuery.from(entityClass);
            return session.createQuery(criteriaQuery).getResultList();
        }
    }

    public String generateId(String prefix) {
        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT MAX(CAST(SUBSTRING(" + getIdColumnName() + ", 5) AS UNSIGNED)) FROM "
                    + entityClass.getSimpleName();
            Integer maxId = session.createNativeQuery(query, Integer.class).getSingleResult();
            int newId = (maxId == null) ? 1 : maxId + 1;
            return String.format("%s-%03d", prefix, newId);
        }
    }

    private String getIdColumnName() {
        return entityClass.getSimpleName().toLowerCase() + "_id";
    }
}