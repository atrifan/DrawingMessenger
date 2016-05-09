package ro.atrifan.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import ro.atrifan.model.*;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.atrifan.persistence.dao.DashBoardDao;
import ro.atrifan.persistence.dao.GroupDao;
import ro.atrifan.persistence.entities.DashBoard;
import ro.atrifan.persistence.entities.Group;
import ro.atrifan.server.util.QueueProducer;

import javax.jms.JMSException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Path("/")
public class DashBoardController {

    @Autowired
    private DashBoardDao dashBoardDao;
    @Autowired
    private GroupDao groupDao;
    private QueueProducer queueProducer;
    private ObjectMapper objectMapper = new ObjectMapper();

    Logger LOG = Logger.getLogger(DashBoardController.class);

    public DashBoardController() {
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
    @Path("/{groupId}/drawing")
    public Response drawing(@PathParam("groupId") String groupId, DrawingModel drawing) {

        LOG.info(String.format("Adding drawing to group <{%s}>", groupId));
        try {
            Group group = groupDao.findByName(groupId);
            DashBoard existingDashBoard = dashBoardDao.findByGroupName(groupId);
            DrawingsModel drawingsModel;
            if (existingDashBoard == null) {
                DashBoard newDashBoard = new DashBoard();
                drawingsModel = new DrawingsModel();
                drawingsModel.addDrawing(drawing);
                newDashBoard.setDrawings(drawingsModel);
                newDashBoard.setMessages(new MessagesModel());
                newDashBoard.setGroup(group);
                dashBoardDao.persist(newDashBoard);
            } else {
                drawingsModel = existingDashBoard.getDrawings();
                drawingsModel.addDrawing(drawing);
                existingDashBoard.setDrawings(drawingsModel);
                dashBoardDao.update(existingDashBoard);
            }

            EventMessage eventMessage = new EventMessage();
            eventMessage.setEventType(Events.DREW);
            eventMessage.setData("x1", drawing.getX1() + "");
            eventMessage.setData("y1", drawing.getY1() + "");
            eventMessage.setData("x2", drawing.getX2() + "");
            eventMessage.setData("y2", drawing.getY2() + "");
            eventMessage.setData("color", drawing.getColor());
            eventMessage.setData("sender", drawing.getSender());
            eventMessage.setData("shape", drawing.getShapeType().getText());
            eventMessage.setData("date", drawing.getCreatedDate() + "");
            eventMessage.setData("group", groupId);
            try {
                queueProducer.produce(eventMessage);
            } catch (JMSException | JsonProcessingException e) {
                LOG.error("Failed to produce drawing message", e);
            }

            LOG.info(String.format("Succesfully added drawing to dashBoard on group <{%s}>", groupId));
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            LOG.error(String.format("Failed to add drawing to group <{%s}>", groupId),
                    ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("/{groupId}")
    public Response info(@PathParam("groupId") String groupId) {
        DashBoardModel dashBoardModel = new DashBoardModel();

        DashBoard dashBoardInfo = dashBoardDao.findByGroupName(groupId);
        if(dashBoardInfo != null) {
            dashBoardModel.setName(groupId);
            dashBoardModel.setDrawingsModel(dashBoardInfo.getDrawings());
            dashBoardModel.setMessagesModel(dashBoardInfo.getMessages());
        }

        return Response.status(Response.Status.OK).entity(dashBoardModel).build();
    }

    @POST
    @Path("/{groupId}/message")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGroup(@PathParam("groupId") String groupId, MessageModel messageModel) {

        LOG.info(String.format("Adding message to group <{%s}>", groupId));
        try {
            Group existingGroup = groupDao.findByName(groupId);
            DashBoard existingDashBoard = dashBoardDao.findByGroupName(groupId);
            MessagesModel messagesModel;
            if(existingDashBoard == null) {
                DashBoard toPersist = new DashBoard();
                messagesModel = new MessagesModel();
                messagesModel.addMessage(messageModel);
                toPersist.setMessages(messagesModel);
                toPersist.setDrawings(new DrawingsModel());
                toPersist.setGroup(existingGroup);
                dashBoardDao.persist(toPersist);
            } else {
                messagesModel = existingDashBoard.getMessages();
                messagesModel.addMessage(messageModel);
                existingDashBoard.setMessages(messagesModel);
                dashBoardDao.update(existingDashBoard);
            }

            LOG.info(String.format("Succesfully added message to dashBoard on group <{%s}>", groupId));
            EventMessage evt = new EventMessage();
            evt.setEventType(Events.MESSAGE);
            evt.setData("message", messageModel.getMessage());
            evt.setData("fontSize", messageModel.getFontSize());
            evt.setData("color", messageModel.getTextColor());
            evt.setData("sender", messageModel.getSender());
            evt.setData("date", messageModel.getDate() + "");
            evt.setData("group", groupId);
            try {
                queueProducer.produce(evt);
            } catch (JMSException e1) {
                LOG.error("Failed to produce chat event", e1);
            } catch (JsonProcessingException e1) {
                LOG.error("Failed to produce chat event", e1);
            }
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            LOG.error(String.format("Failed to add message to group <{%s}>", groupId),
                    ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
