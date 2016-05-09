package ro.atrifan.persistence.dao.impl;

import ro.atrifan.persistence.dao.DashBoardDao;
import ro.atrifan.persistence.dao.GroupDao;
import ro.atrifan.persistence.entities.DashBoard;
import ro.atrifan.persistence.entities.Group;
import ro.atrifan.persistence.entities.GroupUserMap;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class DashBoardDaoImpl extends AbstractDaoImpl<DashBoard> implements DashBoardDao {
    public DashBoard findByGroupName(String group) {
        String queryString = "select d from DashBoard d JOIN d.group g where "
                + ":groupName = g.name";
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("groupName", group);

        DashBoard result;
        try {
            result = (DashBoard) query.getSingleResult();
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }
}
