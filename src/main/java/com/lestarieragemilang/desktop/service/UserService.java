package com.lestarieragemilang.desktop.service;

import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Service class handling user-related operations including authentication and validation.
 * Extends GenericService to inherit basic CRUD operations for User entities.
 */
public class UserService extends GenericService<User> {
    private final SessionFactory sessionFactory;
    
    public UserService() {
        super(new GenericDao<>(User.class), "USR", 10000);
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * Authenticates a user with the given username and password.
     * The password is hashed with BCrypt using the user's salt before comparison.
     *
     * @param username The username to authenticate
     * @param password The plain text password to verify
     * @return The authenticated User object if successful, null otherwise
     */
    public User authenticate(String username, String password) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.where(cb.equal(root.get("username"), username));
            
            User user = session.createQuery(query).getSingleResult();
            if (user != null && BCrypt.checkpw(password + user.getSalt(), user.getPasswordHash())) {
                return user;
            }
        } catch (NoResultException e) {
            return null;
        }
        return null;
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(cb.count(root)).where(cb.equal(root.get("username"), username));
            return session.createQuery(query).getSingleResult() > 0;
        }
    }

    /**
     * Checks if an email address already exists in the database.
     *
     * @param email The email address to check
     * @return true if the email exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(cb.count(root)).where(cb.equal(root.get("email"), email));
            return session.createQuery(query).getSingleResult() > 0;
        }
    }
}