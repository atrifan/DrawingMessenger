package ro.atrifan.server.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ro.atrifan.model.GroupModel;
import ro.atrifan.model.GroupsModel;
import ro.atrifan.model.UserModel;
import ro.atrifan.model.UsersModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.atrifan.persistence.dao.GroupDao;
import ro.atrifan.persistence.dao.GroupUserDao;
import ro.atrifan.persistence.dao.UserDao;
import ro.atrifan.persistence.entities.Group;
import ro.atrifan.persistence.entities.GroupUserMap;
import ro.atrifan.persistence.entities.User;
import ro.atrifan.server.util.QueueProducer;

import javax.jms.JMSException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Path("/group")
public class GroupController {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupUserDao groupUserDao;

    private QueueProducer queueProducer;
    private Logger LOG = Logger.getLogger(GroupController.class);

    public GroupController() {
        try {
            queueProducer = QueueProducer.getProducer();
        } catch (IOException e) {
            LOG.error("Failed to initiate queueProducer", e);
        } catch (JMSException e) {
            LOG.error("Failed to initiate queueProducer", e);
        }
    }
    @GET
    @Produces("application/json")
    public Response getGroups() {
        GroupsModel response = new GroupsModel();
        for(Group entry : groupDao.findAll()) {
            GroupModel group = new GroupModel();
            group.setName(entry.getName());
            response.addGroup(group);
        }
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{groupId}")
    public Response create(@PathParam("groupId") String groupId) {

        try {
            Group foundGroupInDb = groupDao.findByName(groupId);
            if(foundGroupInDb == null) {
                Group toPersist = new Group();
                toPersist.setName(groupId);
                groupDao.persist(toPersist);
                LOG.info(String.format("Sucessfully created group <{%s}>", groupId));
                EventMessage event = new EventMessage();
                event.setData("group", groupId);
                event.setEventType(Events.ADD_GROUP);
                queueProducer.produce(event);
                return Response.status(Response.Status.OK).build();
            } else {
                LOG.error(String.format("Failed to create group <{%s}> because a group with this name is" +
                        " already registerd", groupId));
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        } catch (Exception ex) {
            LOG.error("Failed to register group " + groupId, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }


    @GET
    @Path("/{groupId}")
    @Produces("application/json")
    public Response info(@PathParam("groupId") String groupId) {
        LOG.info(String.format("Getting connected users to group <{%s}>", groupId));
        UsersModel response = new UsersModel();
        for(User entry : groupUserDao.getSubscribedUsersToGroup(groupId)) {
            UserModel user = new UserModel();
            user.setUserName(entry.getUserName());
            user.setActivityState(entry.getState());
            user.setColor(entry.getColor());
            response.addUser(user);
        }
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @GET
    @Path("/{groupId}/free")
    @Produces("application/json")
    public Response getUsersNotOnGroup(@PathParam("groupId") String groupId) {
        LOG.info(String.format("Getting connected users to group <{%s}>", groupId));
        UsersModel response = new UsersModel();
        for(User user : userDao.findAll()) {
            boolean found = false;
            for (User entry : groupUserDao.getSubscribedUsersToGroup(groupId)) {
                if(entry.getUserName().equals(user.getUserName())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                UserModel userToAdd = new UserModel();
                userToAdd.setUserName(user.getUserName());
                userToAdd.setActivityState(user.getState());
                response.addUser(userToAdd);
            }
        }
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @PUT
    @Path("/{groupId}/{userName}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGroup(@PathParam("groupId") String groupId, @PathParam("userName") String userName) {

        GroupUserMap entry = new GroupUserMap();
        entry.setGroup(groupDao.findByName(groupId));
        entry.setUser(userDao.findByName(userName));
        try {
            List<GroupUserMap> results = groupUserDao.getByGroupNameAndUserName(groupId, userName);
            if(results.size() == 0) {
                groupUserDao.persist(entry);
                LOG.info(String.format("Connected user <{%s}> to group <{%s}>", userName, groupId));
                EventMessage event = new EventMessage();
                event.setData("group", groupId);
                event.setData("user", userName);
                event.setEventType(Events.ADD_USER);
                queueProducer.produce(event);
            }
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            LOG.error(String.format("Failed to connect user <{%s]> to group <{%s}> ", userName, groupId),
                    ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
