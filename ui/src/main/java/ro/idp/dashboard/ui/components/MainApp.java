package ro.idp.dashboard.ui.components;

import org.apache.log4j.Logger;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;
import ro.idp.dashboard.util.Session;

import javax.swing.*;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by alexandru.trifan on 08.05.2016.
 */
public class MainApp extends JPanel implements Observer {

    UserPanel userPanel;
    GroupPanel groupPanel;
    MyGroups myGroups;
    Toolbar toolbar;
    Drawer drawer;

    Logger LOG = Logger.getLogger(MainApp.class);

    public MainApp(Map<String, String> message) {
        initAppUI(message);
    }

    private void initAppUI(Map<String, String> data) {
        setSize(800, 700);
        setLayout(null);
        toolbar = new Toolbar();
        userPanel = new UserPanel();
        groupPanel = new GroupPanel();
        myGroups = new MyGroups();
        drawer = new Drawer();
        setPositionOfToolbar();
        setPositionOfUserPanel();
        setPositionOfGroupPanel();
        setPositionOfMyGroups();
        setPositionOfDrawer();
        add(toolbar);
        add(userPanel);
        add(groupPanel);
        add(myGroups);
        add(drawer);
        setVisible(true);
    }

    private void refresh() {
        invalidate();
        validate();
        repaint();
    }



    private void setPositionOfDrawer() {
        int yPosition = myGroups.getY() + myGroups.getHeight() + 10,
                xPosition = myGroups.getX();
        System.out.println(yPosition);
        drawer.setBounds(xPosition, yPosition, drawer.getWidth(), drawer.getHeight());
    }

    private void setPositionOfToolbar() {
        System.out.println(toolbar.getWidth());
        int leftAlignment = getWidth() - toolbar.getWidth() - 10,
                topAlignment = 10,
                width = toolbar.getWidth(),
                height = toolbar.getHeight();
        toolbar.setBounds(leftAlignment, topAlignment, width, height);
    }
    private void setPositionOfUserPanel() {
        userPanel.setBounds(10, 10, userPanel.getWidth(), userPanel.getHeight());
    }
    private void setPositionOfGroupPanel() {
        groupPanel.setBounds(10, userPanel.getHeight() + 10, groupPanel.getWidth(), groupPanel.getHeight());
    }

    private void setPositionOfMyGroups() {
        myGroups.setBounds(toolbar.getX() - 20, toolbar.getY() + toolbar.getHeight(), myGroups.getWidth(),
                myGroups.getHeight());
    }


    @Override
    public void update(Observable o, Object arg) {
        EventMessage message = (EventMessage) arg;
        Events eventType = message.getEventType();

        switch (eventType) {
            case ADD_USER:
                LOG.info("Added new user to group");
                String group = message.getData().get("group"),
                        user = message.getData().get("user");
                if (user.equals(Session.getCurrentUser())) {
                    myGroups.addMyGroup(group);
                }
                break;
            case REDRAW:
                refresh();
                break;
            default:
                break;
        }
    }
}
