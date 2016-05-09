package ro.atrifan.persistence.dao;

import ro.atrifan.persistence.entities.Group;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public interface GroupDao extends AbstractDao<Group> {

    public Group findByName(String groupName);
}
