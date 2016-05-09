package ro.atrifan.persistence.entities;


import ro.atrifan.model.ActivityState;

import javax.persistence.*;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Entity
@Table(name="Utilizator")
public class User {

    @Id
    @GeneratedValue
    private long id;

    @Column(unique=true)
    private String userName;

    private String password;
    private String color;

    private ActivityState state;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ActivityState getState() {
        return state;
    }

    public void setState(ActivityState state) {
        this.state = state;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
