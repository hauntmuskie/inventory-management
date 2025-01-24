package com.lestarieragemilang.desktop.service;

import java.util.List;
import java.util.ArrayList;

import org.hibernate.exception.ConstraintViolationException;

import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import org.hibernate.SessionFactory;

/**
 * Generic service class providing common CRUD operations for entities.
 * Handles database operations and scene cache invalidation.
 *
 * @param <T> The entity type this service manages
 */
public class GenericService<T> {
    private final GenericDao<T> dao;
    private final String idPrefix;
    private final int bound;
    private final SessionFactory sessionFactory;
    private final String[] affectedScenes;

    /**
     * Creates a new GenericService instance.
     *
     * @param dao The data access object for the entity type
     * @param idPrefix Prefix used for ID generation
     * @param bound Upper bound for random ID generation
     * @param affectedScenes Array of scene names that should be invalidated on data changes
     */
    public GenericService(GenericDao<T> dao, String idPrefix, int bound, String... affectedScenes) {
        this.dao = dao;
        this.idPrefix = idPrefix;
        this.bound = bound;
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.affectedScenes = affectedScenes;
    }

    /**
     * Invalidates all cache for known scenes to ensure data consistency.
     */
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

    /**
     * Saves a new entity to the database and invalidates affected caches.
     *
     * @param entity The entity to save
     */
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
