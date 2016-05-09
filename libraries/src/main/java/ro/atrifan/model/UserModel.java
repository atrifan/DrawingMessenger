package ro.atrifan.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexandru.trifan on 25.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel implements Serializable{
    private String userName;
    private ActivityState activityState;
    private String color;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ActivityState getActivityState() {
        return activityState;
    }

    public void setActivityState(ActivityState activityState) {
        this.activityState = activityState;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

