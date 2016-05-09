package ro.idp.dashboard.ui.components;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.GroupModel;
import ro.atrifan.model.GroupsModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.UserService;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;
import ro.idp.dashboard.util.Session;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/24/2016.
 */
public class MyGroups extends JPanel implements Observer{

    JTabbedPane myGroups;
    private UserService userService = UserService.getInstance();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Logger LOG = Logger.getLogger(MyGroups.class);
    private MessageObservable messageObservable = MessageObservable.getInstance();

    public MyGroups() {
        initUI();
    }

    private void initUI() {
        setVisible(false);
        setSize(400, 20);
        myGroups = new JTabbedPane();
        setLayout(null);
        myGroups.setBounds(5, 0, 395, 20);
        messageObservable.addObserver(this);
        add(myGroups);
        myGroups.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                myGroups.getSelectedComponent();
                int index = myGroups.getSelectedIndex();
                String group = myGroups.getTitleAt(index);
                Session.setViewingGroup(group);
                EventMessage eventMessage = new EventMessage();
                eventMessage.setEventType(Events.CHANGE_CONTEXT);
                eventMessage.setData("group", group);
                MessageObservable.getInstance().changeData(eventMessage);
            }
        });
        addMyGroups();
        setVisible(true);
    }

    private void addMyGroups() {
        //TODO: via userName
        getGroups();
    }

    private void getGroups() {
        AbstractWorker abstractWorker = new AbstractWorker();
        abstractWorker.execute();
    }



    public void addMyGroup(String group) {
        JButton button = new JButton(group);
        myGroups.addTab(group, new ImageIcon("yourFile.gif"), button, group);
        if(myGroups.getTabCount() == 1) {
            Session.setViewingGroup(group);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        EventMessage eventMessage = (EventMessage) arg;
        if(eventMessage.getEventType() == Events.ADD_USER) {
            String user = eventMessage.getData().get("user");
            if(user.equals(Session.getCurrentUser())) {
                addMyGroup(eventMessage.getData().get("group"));
            }
        }
    }

    private class AbstractWorker extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return  userService.getSubscribedGroupsOfUser(Session.getCurrentUser(), null);
        }

        protected void done() {
            try {
                HTTPResponse response = get();
                String body = response.getBody();
                GroupsModel groups = objectMapper.readValue(body, GroupsModel.class);
                List<GroupModel> groupsList = groups.getGroups();
                for(int i = 0; i < groupsList.size(); i++) {
                    String group = groupsList.get(i).getName();
                    addMyGroup(group);
                }
            } catch (IOException e) {
                LOG.error("Failed to get groups", e);
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
}
