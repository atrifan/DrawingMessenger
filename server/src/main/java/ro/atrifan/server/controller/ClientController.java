package ro.atrifan.server.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ro.atrifan.model.*;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.atrifan.persistence.dao.GroupUserDao;
import ro.atrifan.persistence.dao.UserDao;
import ro.atrifan.model.ActivityState;
import ro.atrifan.persistence.entities.Group;
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
@Path("/client")
public class ClientController {

    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupUserDao groupUserDao;

    private QueueProducer queueProducer;
    private Logger LOG = Logger.getLogger(ClientController.class);

    public ClientController() {
        try {
            queueProducer = QueueProducer.getProducer();
        } catch (IOException e) {
            LOG.error("Failed to instantiate queue producer", e);
        } catch (JMSException e) {
            LOG.error("Failed to instantiate queue producer", e);
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response login(LoginModel loginModel) {

        try {
            User userFromDB = userDao.findByName(loginModel.getUserName());
            if(userFromDB == null) {
                User userToPersist = new User();
                userToPersist.setState(ActivityState.CONNECTED);
                userToPersist.setUserName(loginModel.getUserName());
                userToPersist.setPassword(loginModel.getPassword());
                userToPersist.setColor(loginModel.getColor());
                userDao.persist(userToPersist);
                EventMessage eventMessage = new EventMessage();
                eventMessage.setEventType(Events.LOGGED_IN);
                eventMessage.setData("userName", loginModel.getUserName());
                UserModel userModel = new UserModel();
                userModel.setUserName(userToPersist.getUserName());
                userModel.setActivityState(userToPersist.getState());
                userModel.setColor(userToPersist.getColor());
                queueProducer.produce(eventMessage);
                LOG.info(String.format("Persisted user <{%s}>", loginModel.getUserName()));
                return Response.status(Response.Status.OK).entity(userModel).build();
            } else {
                LOG.info(String.format("User <{%s}> found in database trying to login", userFromDB.getUserName()));
                if(!userFromDB.getPassword().equals(loginModel.getPassword())) {
                    LOG.info(String.format("Password for user <{%s}> is wrong", userFromDB.getUserName()));
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                } else if(userFromDB.getState() == ActivityState.CONNECTED) {
                    LOG.warn(String.format("User <{%s}> is connected failing connection", userFromDB.getUserName()));
                    return Response.status(Response.Status.FOUND).build();
                } else {
                    LOG.info(String.format("Successfully logged in <{%s}>", userFromDB.getUserName()));
                    userFromDB.setState(ActivityState.CONNECTED);
                    userDao.update(userFromDB);
                    EventMessage eventMessage = new EventMessage();
                    eventMessage.setEventType(Events.LOGGED_IN);
                    eventMessage.setData("userName", loginModel.getUserName());
                    queueProducer.produce(eventMessage);
                    UserModel userModel = new UserModel();
                    userModel.setUserName(userFromDB.getUserName());
                    userModel.setActivityState(userFromDB.getState());
                    userModel.setColor(userFromDB.getColor());
                    return Response.status(Response.Status.OK).entity(userModel).build();
                }
            }
        } catch (Exception ex) {
            LOG.error("Failed to save/login client " + loginModel.getUserName(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("/{userName}/groups")
    @Produces("application/json")
    public Response getGroupsOfUser(@PathParam("userName") String userName) {
        LOG.info(String.format("Getting subscribed groups of user <{%s}>", userName));
        try {
            List<Group> groups = groupUserDao.getSubscribedGroupsOfUser(userName);
            GroupsModel response = new GroupsModel();
            for(Group group : groups) {
                GroupModel groupModel = new GroupModel();
                groupModel.setName(group.getName());
                response.addGroup(groupModel);
            }
            LOG.info(String.format("Retrived subscribed groups of user <{%s}>", userName));
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (Exception ex) {
            LOG.error(String.format("Failed to get subscribed groups of user <{%s}>", userName), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{userName}/logout")
    @Produces("application/json")
    public Response logout(@PathParam("userName") String userName) {
        LOG.info(String.format("Logging out user <{%s}>", userName));
        try {
            User userToLogOut = userDao.findByName(userName);
            userToLogOut.setState(ActivityState.OFFLINE);
            userDao.update(userToLogOut);
            LOG.info(String.format("User <{%s}> was successfully logged out", userName));
            EventMessage eventMessage = new EventMessage();
            eventMessage.setEventType(Events.LOGGED_OUT);
            eventMessage.setData("userName", userName);
            queueProducer.produce(eventMessage);
            return Response.status(Response.Status.OK).build();
        } catch(Exception ex) {
            LOG.error("Failed to logout user " + userName, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getAllUsers() {

        UsersModel response = new UsersModel();
        for(User user : userDao.findAll()) {
            UserModel userModel = new UserModel();
            userModel.setUserName(user.getUserName());
            userModel.setActivityState(user.getState());
            response.addUser(userModel);
        }
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") String userId) {

        UserModel response = new UserModel();
        try {
            User user = userDao.findByName(userId);
            if (user != null) {
                response.setColor(user.getColor());
                response.setActivityState(user.getState());
                response.setUserName(user.getUserName());
            }
        } catch (Exception ex) {
            LOG.error("Failed to retrieve user ", ex);
        }
        return Response.status(Response.Status.OK).entity(response).build();
    }


}
