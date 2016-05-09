package ro.atrifan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 25.04.2016.
 */
public class GroupsModel {
    private List<GroupModel> groups = new ArrayList<GroupModel>();

    public List<GroupModel> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupModel> groups) {
        this.groups = groups;
    }

    public void addGroup(GroupModel groupModel) {
        this.groups.add(groupModel);
    }
}
