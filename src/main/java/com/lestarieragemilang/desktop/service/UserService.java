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

public class UserService extends GenericService<User> {
    private final SessionFactory sessionFactory;
    
    public UserService() {
        super(new GenericDao<>(User.class), "USR", 10000);
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

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

    public boolean isUsernameExists(String username) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(cb.count(root)).where(cb.equal(root.get("username"), username));
            return session.createQuery(query).getSingleResult() > 0;
        }
    }

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