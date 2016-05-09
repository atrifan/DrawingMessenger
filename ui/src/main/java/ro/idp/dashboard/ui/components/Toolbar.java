package ro.idp.dashboard.ui.components;

import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.UserService;
import ro.idp.dashboard.util.Session;

import javax.swing.*;
import javax.ws.rs.core.Response;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/23/2016.
 */
public class Toolbar extends JPanel implements Observer{
    JLabel helloMessage = new JLabel("Logged as: %s");
    JButton logout = new JButton("Log Out");
    JButton saveGroupWork = new JButton("Save Group Work");
    JButton createGroup = new JButton("Create Group");
    private JTextField groupName = new JTextField();
    UserService userService = UserService.getInstance();
    Logger LOG = Logger.getLogger(Toolbar.class);
    MessageObservable messageObservable = MessageObservable.getInstance();

    public Toolbar() {
        initUI();
        messageObservable.addObserver(this);
    }

    private void initUI() {
        setSize(420, 120);
        setLayout(null);
        String text = String.format(helloMessage.getText(), Session.getCurrentUser());
        helloMessage.setText(text);
        helloMessage.setBounds(160, 10, 110, 20);
        helloMessage.setHorizontalAlignment(SwingConstants.RIGHT);

        logout.setBounds(300, 10, 100, 20);
        saveGroupWork.setBounds(40, 60, 180, 20);
        createGroup.setBounds(40 + saveGroupWork.getWidth() + 10, 60, 110, 20);
        groupName.setBounds(40, 90, saveGroupWork.getWidth() + createGroup.getWidth(), 20);
        add(helloMessage);
        add(logout);
        add(saveGroupWork);
        add(createGroup);
        add(groupName);
        setVisible(true);
        logout.addActionListener(logOut());
        createGroup.addActionListener(createGroup());
        saveGroupWork.addActionListener(saveGroupWork());
    }

    private ActionListener createGroup() {
        ActionListener handler = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                groupName.setEditable(false);
                EventMessage eventMessage = new EventMessage();
                eventMessage.setEventType(Events.CREATE_GROUP);
                eventMessage.setData("group", groupName.getText());
                MessageObservable.getInstance().changeData(eventMessage);
            }
        };

        return handler;
    }

    private ActionListener logOut() {
        ActionListener handler = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractLogoutWorker abstractLogoutWorker = new AbstractLogoutWorker();
                abstractLogoutWorker.execute();
            }
        };

        return handler;
    }

    private ActionListener saveGroupWork() {
        ActionListener handler = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EventMessage eventMessage = new EventMessage();
                eventMessage.setEventType(Events.SAVE_IMAGE);
                MessageObservable.getInstance().changeData(eventMessage);
            }
        };

        return handler;
    }

    private class AbstractLogoutWorker extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return userService.logout(Session.getCurrentUser(), null);
        }

        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info(String.format("Successfully logged out <{%s}>", Session.getCurrentUser()));
                        EventMessage message = new EventMessage();
                        message.setEventType(Events.LOGOUT);
                        messageObservable.changeData(message);
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.info(String.format("Failed to logout <{%s}>", Session.getCurrentUser()));
                        break;
                    default:
                        break;
                }
                EventMessage eventMessage = new EventMessage();
                eventMessage.setEventType(Events.REDRAW);
                messageObservable.changeData(eventMessage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        EventMessage eventMessage = (EventMessage) arg;

        if(eventMessage.getEventType() == Events.ADD_GROUP) {
            if(eventMessage.getData().get("group").equals(groupName.getText())) {
                EventMessage linkage = new EventMessage();
                linkage.setEventType(Events.LINK_USER);
                linkage.setData("group", groupName.getText());
                linkage.setData("user", Session.getCurrentUser());
                MessageObservable.getInstance().changeData(linkage);
                groupName.setEditable(true);
                groupName.setText("");
            }
        }
    }
}
