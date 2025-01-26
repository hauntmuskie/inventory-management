package com.lestarieragemilang.desktop.service;

import java.util.List;
import java.util.ArrayList;

import org.hibernate.exception.ConstraintViolationException;

import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import org.hibernate.SessionFactory;

/**
 * Generic service class providing common CRUD (Create, Read, Update, Delete)
 * operations for entities.
 * This class serves as a base service layer implementation that handles:
 * - Database operations through a generic DAO
 * - Scene cache invalidation for UI updates
 * - ID generation for new entities
 * - Database availability checks
 *
 * @param <T> The entity type this service manages (e.g., Product, Customer,
 *            Order)
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
     * @param dao            The data access object for the entity type
     * @param idPrefix       Prefix used for ID generation (e.g., "PROD" for
     *                       products)
     * @param bound          Upper bound for random ID generation (e.g., 1000 for
     *                       IDs up to 999)
     * @param affectedScenes Array of scene names that should be invalidated on data
     *                       changes
     *                       to ensure UI consistency
     */
    public GenericService(GenericDao<T> dao, String idPrefix, int bound, String... affectedScenes) {
        this.dao = dao;
        this.idPrefix = idPrefix;
        this.bound = bound;
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.affectedScenes = affectedScenes;
    }

    /**
     * Invalidates cache for all known scenes to ensure data consistency across the
     * application.
     * This is typically called after database operations that might affect multiple
     * views.
     */
    private void invalidateAllCaches() {
        if (App.sceneManager != null) {
            App.sceneManager.invalidateScenes(
                    "stokbesi",
                    "kategori",
                    "transaksi",
                    "retur",
                    "supplier",
                    "customer");
        }
    }

    /**
     * Saves a new entity to the database and updates the UI cache.
     * If the database is unavailable, the operation is skipped silently.
     *
     * @param entity The entity to save
     * @throws RuntimeException if there's an error during the save operation
     */
    public void save(T entity) {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.save(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    /**
     * Updates an existing entity in the database and refreshes the UI cache.
     * If the database is unavailable, the operation is skipped silently.
     *
     * @param entity The entity to update
     * @throws RuntimeException if there's an error during the update operation
     */
    public void update(T entity) {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.update(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    /**
     * Checks if an entity can be safely deleted without violating any constraints.
     *
     * @param entity The entity to check
     * @return true if the entity can be deleted, false if it would violate
     *         constraints
     */
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

    /**
     * Deletes an entity from the database and updates the UI cache.
     * If the database is unavailable, the operation is skipped silently.
     *
     * @param entity The entity to delete
     * @throws ConstraintViolationException if deletion would violate database
     *                                      constraints
     */
    public void delete(T entity) throws ConstraintViolationException {
        if (!HibernateUtil.isDatabaseAvailable())
            return;
        dao.delete(entity);
        invalidateAllCaches();
        invalidateAffectedScenes();
    }

    /**
     * Retrieves an entity by its ID.
     *
     * @param id The ID of the entity to find
     * @return The found entity or null if not found or if database is unavailable
     */
    public T findById(Long id) {
        if (!HibernateUtil.isDatabaseAvailable())
            return null;
        return dao.findById(id);
    }

    /**
     * Retrieves all entities of type T from the database.
     *
     * @return List of all entities, or empty list if database is unavailable
     */
    public List<T> findAll() {
        if (!HibernateUtil.isDatabaseAvailable())
            return new ArrayList<>();
        return dao.findAll();
    }

    /**
     * Generates a unique ID for a new entity using the configured prefix and bound.
     * Format: prefix + random number (e.g., "PROD123")
     *
     * @return A new unique identifier string
     */
    public String generateId() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            return idPrefix + (int) (Math.random() * bound);
        }
        return dao.generateId(idPrefix, bound);
    }

    /**
     * Returns the Hibernate SessionFactory used by this service.
     * Primarily used by subclasses that need direct database access.
     *
     * @return The configured Hibernate SessionFactory instance
     */
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Invalidates the cache for scenes that are specifically affected by
     * changes to this entity type.
     */
    private void invalidateAffectedScenes() {
        if (App.sceneManager != null && affectedScenes != null && affectedScenes.length > 0) {
            App.sceneManager.invalidateScenes(affectedScenes);
        }
    }
}
