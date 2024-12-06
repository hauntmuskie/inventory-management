package com.lestarieragemilang.desktop.service;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;

import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import org.hibernate.SessionFactory;

public class GenericService<T> {
    private final GenericDao<T> dao;
    private final String idPrefix;
    private final int bound;
    private final SessionFactory sessionFactory;

    public GenericService(GenericDao<T> dao, String idPrefix, int bound) {
        this.dao = dao;
        this.idPrefix = idPrefix;
        this.bound = bound;
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public void save(T entity) {
        dao.save(entity);
    }

    public void update(T entity) {
        dao.update(entity);
    }

    public boolean canDelete(T entity) {
        try {
            dao.checkDeleteConstraints(entity);
            return true;
        } catch (ConstraintViolationException e) {
            return false;
        }
    }

    public void delete(T entity) throws ConstraintViolationException {
        dao.delete(entity);
    }

    public T findById(Long id) {
        return dao.findById(id);
    }

    public List<T> findAll() {
        return dao.findAll();
    }

    public String generateId() {
        return dao.generateId(idPrefix, bound);
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
