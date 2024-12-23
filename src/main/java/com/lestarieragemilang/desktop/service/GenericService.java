package com.lestarieragemilang.desktop.service;

import java.util.List;
import java.util.ArrayList;

import org.hibernate.exception.ConstraintViolationException;

import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import org.hibernate.SessionFactory;

public class GenericService<T> {
    private final GenericDao<T> dao;
    private final String idPrefix;
    private final int bound;
    private final SessionFactory sessionFactory;
    private final String[] affectedScenes;

    public GenericService(GenericDao<T> dao, String idPrefix, int bound, String... affectedScenes) {
        this.dao = dao;
        this.idPrefix = idPrefix;
        this.bound = bound;
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.affectedScenes = affectedScenes;
    }

    private void invalidateAllCaches() {
        // Invalidate all known scenes to ensure data consistency across the application
        App.sceneManager.invalidateScenes(
            "stokbesi", 
            "kategori", 
            "transaksi", 
            "retur", 
            "supplier", 
            "customer"
        );
    }

    public void save(T entity) {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.save(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    public void update(T entity) {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.update(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    public boolean canDelete(T entity) {
        if (!HibernateUtil.isDatabaseAvailable())
            return false;
        try {
            dao.checkDeleteConstraints(entity);
            return true;
        } catch (ConstraintViolationException e) {
            return false;
        }
    }

    public void delete(T entity) throws ConstraintViolationException {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.delete(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    public T findById(Long id) {
        if (!HibernateUtil.isDatabaseAvailable())
            return null;
        return dao.findById(id);
    }

    public List<T> findAll() {
        if (!HibernateUtil.isDatabaseAvailable())
            return new ArrayList<>();
        return dao.findAll();
    }

    public String generateId() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            return idPrefix + (int) (Math.random() * bound);
        }
        return dao.generateId(idPrefix, bound);
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private void invalidateAffectedScenes() {
        if (affectedScenes != null && affectedScenes.length > 0) {
            App.sceneManager.invalidateScenes(affectedScenes);
        }
    }
}
