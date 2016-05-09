package ro.atrifan.persistence.dao;

import ro.atrifan.persistence.entities.User;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public interface UserDao extends AbstractDao<User>{

    public User findByNameAndPassword(String name, String password);
    public User findByName(String name);
}
