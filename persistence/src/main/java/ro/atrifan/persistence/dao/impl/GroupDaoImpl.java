package ro.atrifan.persistence.dao.impl;

import ro.atrifan.persistence.dao.GroupDao;
import ro.atrifan.persistence.entities.Group;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Transactional
public class GroupDaoImpl extends AbstractDaoImpl<Group> implements GroupDao {

    public GroupDaoImpl() {
        super(Group.class);
    }

    public Group findByName(String groupName) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> c = cb.createQuery(Group.class);
        Root<Group> groupRoot = c.from(Group.class);
        c.select(groupRoot);
        c.where(cb.equal(groupRoot.get("name"), groupName));
        Group result;
        try {
            result = this.entityManager.createQuery(c).getSingleResult();
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }
}

