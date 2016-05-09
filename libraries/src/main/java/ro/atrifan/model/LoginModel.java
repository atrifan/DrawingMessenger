package ro.atrifan.model;

import java.io.Serializable;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class LoginModel implements Serializable {
    private String userName;
    private String password;
    private String color;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
