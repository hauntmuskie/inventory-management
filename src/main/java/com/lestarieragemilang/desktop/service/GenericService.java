package com.lestarieragemilang.desktop.service;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;

import com.lestarieragemilang.desktop.repository.GenericDao;

public class GenericService<T> {
    private final GenericDao<T> dao;
    private final String idPrefix;

    public GenericService(GenericDao<T> dao, String idPrefix) {
        this.dao = dao;
        this.idPrefix = idPrefix;
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
        return dao.generateId(idPrefix);
    }
}
