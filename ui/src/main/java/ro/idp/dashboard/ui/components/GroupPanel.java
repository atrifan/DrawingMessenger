package ro.idp.dashboard.ui.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import ro.atrifan.client.http.util.HTTPParser;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.GroupModel;
import ro.atrifan.model.GroupsModel;
import ro.atrifan.model.UserModel;
import ro.atrifan.model.UsersModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.GroupService;
import org.apache.log4j.Logger;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/23/2016.
 */
public class GroupPanel extends JPanel implements Observer {

    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("groups");
    private DefaultTreeModel treeModel;
    private JTree groups;
    private GroupService groupService = GroupService.getInstance();
    MessageObservable messageObservable = MessageObservable.getInstance();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Logger LOG = Logger.getLogger(GroupPanel.class);

    public GroupPanel() {
        setVisible(false);
        initUI();
        messageObservable.addObserver(this);
        setMouseListener();
        this.getGroups();
        setVisible(true);
    }

    private void setMouseListener() {
        groups.addMouseListener(groupAction());
    }

    private MouseAdapter groupAction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = groups.getPathForLocation(e.getX(), e.getY());
                    if(path.getPathCount() == 2) {
                        showGroupActions(e, path);
                    }
                }
            }
        };
        return mouseAdapter;
    }


    private void showGroupActions(MouseEvent e, TreePath path) {
        Rectangle pathBounds = groups.getUI().getPathBounds(groups, path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String groupName = (String) node.getUserObject();
        getUsersNotInGroup(groupName, pathBounds, e);

    }

    private void getUsersNotInGroup(String group, Rectangle pathBounds, MouseEvent ev) {
        AbstractWorkerUsersNotInGroup abstractWorkerUsersNotInGroup = new AbstractWorkerUsersNotInGroup(group, pathBounds, ev);
        abstractWorkerUsersNotInGroup.execute();
    }


    private void initUI() {
        setSize(300, 380);
        treeModel = new DefaultTreeModel(rootNode);
        groups = new JTree(treeModel);
        groups.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        groups.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(groups);
        scrollPane.setBounds(10, 10, 290, 360);
        add(scrollPane);
        setLayout(null);
    }

    public void getGroups() {
        AbstractWorkerGroup abstractWorkerGroup = new AbstractWorkerGroup();
        abstractWorkerGroup.execute();
    }

    private void getUsers(String groupName) {
        AbstractWorkerUser abstractWorkerUser = new AbstractWorkerUser(groupName);
        abstractWorkerUser.execute();
    }

    public void createGroup(String groupName) throws IOException {
        AbstractWorkerCreateGroup abstractWorkerCreateGroup = new AbstractWorkerCreateGroup(groupName);
        abstractWorkerCreateGroup.execute();
    }

    public void addGroup(String groupName) throws IOException {
        getUsers(groupName);
    }

    public void linkUser(String group, String user) {
        AbstractWorkerLinkage abstractWorkerLinkage = new AbstractWorkerLinkage(group, user);
        abstractWorkerLinkage.execute();
    }


    public void addUser(String group, String user) {
        int noChildren = treeModel.getChildCount(rootNode);
        for(int i = 0; i < noChildren; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getChild(rootNode, i);
            if(node.getUserObject().toString().equals(group)) {
                node.add(new DefaultMutableTreeNode(user));
                treeModel.reload(node);
                groups.expandPath(new TreePath(node.getPath()));
                break;
            }
        }
    }


    private void refresh() {
        invalidate();
        validate();
        repaint();
    }


    @Override
    public void update(Observable o, Object arg) {
        EventMessage message = (EventMessage) arg;
        EventMessage eventMessage = new EventMessage();
        switch (message.getEventType()) {
            case ADD_USER:
                addUser(message.getData().get("group"), message.getData().get("user"));
                eventMessage.setEventType(Events.REDRAW);
                messageObservable.changeData(eventMessage);
                break;
            case ADD_GROUP:
                try {
                    addGroup(message.getData().get("group"));
                    eventMessage.setEventType(Events.REDRAW);
                    messageObservable.changeData(eventMessage);
                } catch (IOException e) {
                    LOG.error("Failed to add group", e);
                }
                break;
            case CREATE_GROUP:
                try {
                    createGroup(message.getData().get("group"));
                } catch (IOException e) {
                    LOG.error("Failed to create group", e);
                }
                break;
            case LINK_USER:
                linkUser(message.getData().get("group"), message.getData().get("user"));
                break;
            default:
                break;
        }
    }


    private class AbstractWorkerGroup extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            LOG.info("Getting groups");
            return groupService.getGroups(null);
        }

        protected void done() {
            try {
                HTTPResponse response = get();
                String body = response.getBody();
                GroupsModel groups = objectMapper.readValue(body, GroupsModel.class);
                List<String> groupsToAdd = new ArrayList<>();
                for(GroupModel entry : groups.getGroups()) {
                    groupsToAdd.add(entry.getName());
                }

                for(String group : groupsToAdd) {
                    LOG.info(String.format("Adding group <{%s}>", group));
                    getUsers(group);
                }
            } catch (IOException e) {
                LOG.error("Failed to get groups", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class AbstractWorkerUser extends SwingWorker<HTTPResponse, Void> {

        private String groupName;
        public AbstractWorkerUser(String groupName) {
            super();
            this.groupName = groupName;
        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return groupService.getUsersOfGroup(groupName, null);
        }

        @Override
        protected void done() {
            try {
                HTTPResponse response = get();
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupName);
                String body = response.getBody();
                UsersModel users = objectMapper.readValue(body, UsersModel.class);
                for(UserModel content : users.getUsers()) {
                    DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(content.getUserName());
                    groupNode.add(userNode);
                }
                treeModel.insertNodeInto(groupNode, rootNode, rootNode.getChildCount());
                groups.scrollPathToVisible(new TreePath(groupNode.getPath()));
                groups.expandPath(new TreePath(groupNode.getPath()));
                LOG.info(String.format("Successfully retrieved userList for group <{%s}>", groupName));
            } catch (java.io.IOException e) {
                LOG.error(String.format("Failed to get users for group <{%s}>", groupName), e);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class AbstractWorkerLinkage extends SwingWorker<HTTPResponse, Void> {

        private String group,
            user;
        public AbstractWorkerLinkage(String group, String user) {
            this.user = user;
            this.group = group;
        }

        @Override
        protected HTTPResponse doInBackground() throws Exception {
           return groupService.addUserToGroup(group, user, null);
        }

        @Override
        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info(String.format("Successfully added user <{%s}> to group <{%s}>", user, group));
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.info(String.format("Failed to add user <{%s}> to group <{%s}>", user, group));
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class AbstractWorkerCreateGroup extends SwingWorker<HTTPResponse, Void> {

        private String groupName;
        public AbstractWorkerCreateGroup(String groupName) {
            this.groupName = groupName;
        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return groupService.createGroup(groupName, null);
        }

        @Override
        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info(String.format("Group <{%s}> created sucessfully", groupName));
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.info(String.format("Failed to create group <{%s}>", groupName));
                        break;
                    case NOT_ACCEPTABLE:
                        LOG.info(String.format("Failed to create group <{%s}> because it already exists", groupName));
                        break;
                }
                refresh();
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

    private class AbstractWorkerUsersNotInGroup extends SwingWorker<HTTPResponse, Void> {

        private String group;
        private Rectangle pathBounds;
        private MouseEvent ev;
        public AbstractWorkerUsersNotInGroup(String group, final Rectangle pathBounds, final MouseEvent ev) {
            this.group = group;
            this.pathBounds = pathBounds;
            this.ev = ev;
        }

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return groupService.getUsersNotInGroup(group, null);
        }

        @Override
        protected void done() {
            try {
                HTTPResponse response = get();
                String body = response.getBody();
                UsersModel usersNotInGroup = objectMapper.readValue(body, UsersModel.class);
                ActionMenu actionMenu = new ActionMenu(group);
                if (pathBounds != null && pathBounds.contains(ev.getX(), ev.getY())) {
                    JPopupMenu menu = new JPopupMenu();
                    actionMenu.addJoinGroupOption(usersNotInGroup);
                    actionMenu.addAddUserOption(usersNotInGroup);
                    actionMenu.initActions();
                    menu.add(actionMenu);
                    menu.show(groups, pathBounds.x + pathBounds.width, pathBounds.y + pathBounds.height);
                }
            } catch (IOException e) {
                LOG.error(String.format("Failed to get users of group <{%s}>", group), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
