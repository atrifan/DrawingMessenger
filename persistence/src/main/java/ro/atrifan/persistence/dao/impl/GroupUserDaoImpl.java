package ro.atrifan.persistence.dao.impl;

import ro.atrifan.persistence.dao.GroupUserDao;
import ro.atrifan.persistence.dao.UserDao;
import ro.atrifan.persistence.entities.Group;
import ro.atrifan.persistence.entities.GroupUserMap;
import ro.atrifan.persistence.entities.User;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class GroupUserDaoImpl extends AbstractDaoImpl<GroupUserMap> implements GroupUserDao {
    public List<Group> getSubscribedGroupsOfUser(String userName) {
        String queryString = "select g from GroupUserMap g JOIN g.user u where "
                + ":userName = u.userName";
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("userName", userName);
        List<GroupUserMap> partialResult = query.getResultList();
        List<Group> groupResult = new ArrayList<Group>();
        for(GroupUserMap entry : partialResult) {
            groupResult.add(entry.getGroup());
        }

        return groupResult;
    }

    public List<User> getSubscribedUsersToGroup(String groupName) {
        String queryString = "select g from GroupUserMap g JOIN g.group gu where "
                + ":name = gu.name";
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("name", groupName);
        List<GroupUserMap> partialResult = query.getResultList();
        List<User> userResult = new ArrayList<User>();
        for(GroupUserMap entry : partialResult) {
            userResult.add(entry.getUser());
        }

        return userResult;
    }

    public List<GroupUserMap> getByGroupNameAndUserName(String groupName, String userName) {
        String queryString = "select g from GroupUserMap g JOIN g.group gu JOIN g.user u where "
                + ":name = gu.name and :userName = u.userName";
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("name", groupName);
        query.setParameter("userName", userName);
        List<GroupUserMap> partialResult = query.getResultList();

        return partialResult;
    }


}
