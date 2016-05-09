package ro.idp.dashboard.ui.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.UserModel;
import ro.atrifan.model.UsersModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.GroupService;
import org.apache.log4j.Logger;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;
import ro.idp.dashboard.util.Session;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.ws.rs.core.Response;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/24/2016.
 */
public class ActionMenu extends JPanel{

    private JTree actions;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("right-click");
    private DefaultTreeModel treeModel;
    private String groupName;
    private GroupService groupService = GroupService.getInstance();
    private Logger LOG = Logger.getLogger(ActionMenu.class);

    public ActionMenu(String groupName) {
        //TODO: pass also the current user
        initUI();
        this.groupName = groupName;
    }

    private void initUI() {
        setSize(240, 120);
        setLayout(null);
        setVisible(true);
        treeModel = new DefaultTreeModel(rootNode);
        actions = new JTree(treeModel);
        actions.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        actions.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(actions);
        scrollPane.setBounds(9, 9, 240 - 18, 120 - 18);
        add(scrollPane);
    }

    public void addAddUserOption(UsersModel usersNotInGroup) {
        DefaultMutableTreeNode addUser = new DefaultMutableTreeNode("Add User");
        for (UserModel user : usersNotInGroup.getUsers()) {
            if(user.getUserName().equals(Session.getCurrentUser())) {
                //If you are not in the group you cannot add members
                return;
            }
            DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(user.getUserName());
            addUser.add(userNode);
        }
        treeModel.insertNodeInto(addUser, rootNode, rootNode.getChildCount());
        actions.expandPath(new TreePath(addUser.getPath()));
    }


    public void addJoinGroupOption(UsersModel usersNotInGroup) {
        int i;

        boolean shouldAdd = false;
        for(i = 0; i < usersNotInGroup.getUsers().size(); i++) {
            if(usersNotInGroup.getUsers().get(i).getUserName().equals(Session.getCurrentUser())) {
                shouldAdd = true;
            }
        }

        if(!shouldAdd) {
            return;
        }
        DefaultMutableTreeNode joinGroup = new DefaultMutableTreeNode("Join Group");
        DefaultMutableTreeNode theGroup = new DefaultMutableTreeNode(groupName);
        joinGroup.add(theGroup);
        treeModel.insertNodeInto(joinGroup, rootNode, rootNode.getChildCount());
        actions.expandPath(new TreePath(joinGroup.getPath()));
    }

    public void initActions() {
        actions.addMouseListener(menuAction());
    }

    private MouseAdapter menuAction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = actions.getPathForLocation(e.getX(), e.getY());
                    if(path.getPathCount() == 3) {
                        triggerAction(path);
                    }
                }
            }
        };
        return mouseAdapter;
    }

    private void triggerAction(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String actionName = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject(),
            actor = (String) node.getUserObject();

        if(actionName.equals("Add User")) {
            AbstractWorkerLinkage abstractWorkerLinkage = new AbstractWorkerLinkage(this.groupName, actor);
            abstractWorkerLinkage.execute();
        } else if(actionName.equals("Join Group")) {
            AbstractWorkerLinkage abstractWorkerLinkage = new AbstractWorkerLinkage(this.groupName, Session.getCurrentUser());
            abstractWorkerLinkage.execute();
        }
    }


    private class AbstractWorkerLinkage extends SwingWorker<HTTPResponse, Void> {

        private String group,
                user;
        public AbstractWorkerLinkage(String user, String group) {
            this.user = user;
            this.group = group;
        }

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return groupService.addUserToGroup(user, group, null);
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
                    case INTERNAL_SERVER_ERROR:
                        LOG.info(String.format("Failed to add user <{%s}> to group <{%s}>", user, group));
                        break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
