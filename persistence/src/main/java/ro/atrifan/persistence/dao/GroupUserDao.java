package ro.atrifan.persistence.dao;

import ro.atrifan.persistence.entities.Group;
import ro.atrifan.persistence.entities.GroupUserMap;
import ro.atrifan.persistence.entities.User;

import java.util.List;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public interface GroupUserDao extends AbstractDao<GroupUserMap>{

    public List<Group> getSubscribedGroupsOfUser(String userName);
    public List<User> getSubscribedUsersToGroup(String groupName);
    public List<GroupUserMap> getByGroupNameAndUserName(String groupName, String userName);
}
