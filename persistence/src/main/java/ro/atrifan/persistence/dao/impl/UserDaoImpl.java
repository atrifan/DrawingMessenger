package ro.atrifan.persistence.dao.impl;

import ro.atrifan.persistence.dao.UserDao;
import ro.atrifan.persistence.entities.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Transactional
public class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    public User findByNameAndPassword(String name, String password) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<User> c = cb.createQuery(User.class);
        Root<User> user = c.from(User.class);
        c.select(user);
        c.where(cb.equal(user.get("userName"), name)).
                where(cb.equal(user.get("password"), password));
        User result;
        try {
            result = this.entityManager.createQuery(c).getSingleResult();
        } catch(Exception ex) {
            result = null;
        }

        return result;
    }

    public User findByName(String name) {
        User result;
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<User> c = cb.createQuery(User.class);
        Root<User> user = c.from(User.class);
        c.select(user);
        c.where(cb.equal(user.get("userName"), name));
        try {
            result = this.entityManager.createQuery(c).getSingleResult();
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }
}