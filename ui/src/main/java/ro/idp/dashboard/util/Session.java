package ro.idp.dashboard.util;

import java.awt.*;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class Session {
    private static String currentUser;
    private static Color userColor;
    private static String userColorName;
    private static String viewingGroup;

    public synchronized static void setCurrentUser(String userName) {
        currentUser = userName;
    }

    public synchronized static void setViewingGroup(String group) {
        viewingGroup = group;
    }

    public synchronized static void setUserColor(Color color) {
        userColor = color;
    }

    public static String getViewingGroup() {
        return viewingGroup;
    }

    public synchronized static Color getUserColor() {
        return userColor;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public synchronized static String getUserColorName() {
        return userColorName;
    }

    public synchronized static void setUserColorName(String userColorName) {
        Session.userColorName = userColorName;
        Session.userColor = Util.getColorByString(userColorName);
    }
}
