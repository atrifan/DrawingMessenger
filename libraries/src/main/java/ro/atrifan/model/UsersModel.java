package ro.atrifan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 25.04.2016.
 */
public class UsersModel {

    private List<UserModel> users = new ArrayList<UserModel>();

    public List<UserModel> getUsers() {
        return users;
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }

    public void addUser(UserModel user) {
        users.add(user);
    }
}
