package com.lestarieragemilang.desktop.service;

import java.util.List;

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

    public void delete(T entity) {
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
