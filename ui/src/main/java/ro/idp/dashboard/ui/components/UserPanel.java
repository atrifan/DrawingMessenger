package ro.idp.dashboard.ui.components;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.UserModel;
import ro.atrifan.model.UsersModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.atrifan.model.ActivityState;
import ro.idp.dashboard.connector.http.UserService;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/23/2016.
 */
public class UserPanel extends JPanel implements Observer{

    DefaultListModel listOfUsers = new DefaultListModel();
    UserService userService = UserService.getInstance();
    Logger LOG = Logger.getLogger(UserPanel.class);
    ObjectMapper objectMapper = new ObjectMapper();
    MessageObservable messageObservable = MessageObservable.getInstance();

    public UserPanel() {
        initUI();
        messageObservable.addObserver(this);
        getConnectedUsers();
    }

    private void initUI() {
        setSize(300, 180);
        JList users = new JList(listOfUsers);
        JScrollPane scrollPane = new JScrollPane(users);
        scrollPane.setBounds(10, 10, 290, 160);
        add(scrollPane);
        setLayout(null);
        setVisible(true);
    }

    public void addUser(String user) {
        if(!listOfUsers.contains(user)) {
            listOfUsers.addElement(user);
        }
    }

    public void removeUser(String user) {
        listOfUsers.removeElement(user);
    }

    private void refresh() {
        invalidate();
        validate();
        repaint();
    }

    public void getConnectedUsers() {
        GetUsersWorker getUsersWorker = new GetUsersWorker();
        getUsersWorker.execute();
    }

    private class GetUsersWorker extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return userService.getUsers(null);
        }

        @Override
        protected void done() {
            LOG.info("Got connectd users of app, adding them");
            try {
                HTTPResponse response = get();
                String body = response.getBody();
                UsersModel users = objectMapper.readValue(body, UsersModel.class);
                List<String> usersToAdd = new ArrayList<>();
                for(UserModel entry : users.getUsers()) {
                    if(entry.getUserName().equals(getName()) ||
                            entry.getActivityState() == ActivityState.OFFLINE) {
                        continue;
                    }
                    addUser(entry.getUserName());
                }
            } catch (IOException e) {
                LOG.error("Failed to get users", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            EventMessage eventMessage = new EventMessage();
            eventMessage.setEventType(Events.REDRAW);
            messageObservable.changeData(eventMessage);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        EventMessage message = (EventMessage) arg;
        if(message.getEventType() == Events.LOGGED_IN) {
            addUser(message.getData().get("userName"));
        } else if(message.getEventType() == Events.LOGGED_OUT) {
            removeUser(message.getData().get("userName"));
        }
    }
}
