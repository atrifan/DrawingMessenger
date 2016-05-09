package ro.idp.dashboard.ui.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.model.DashBoardModel;
import ro.atrifan.model.DrawingModel;
import ro.atrifan.model.ShapeSelection;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.DashBoardService;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.MessageQueueWorker;
import ro.idp.dashboard.util.Session;
import ro.idp.dashboard.util.Util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Enti on 2/23/2016.
 */
public class Drawer extends JPanel implements Observer {
    private Chat myChat;
    private DrawArea drawArea;
    MessageObservable messageObservable = MessageObservable.getInstance();
    DashBoardService dashBoardService = DashBoardService.getInstance();
    private ObjectMapper objectMapper = new ObjectMapper();
    Logger LOG = Logger.getLogger(Drawer.class);

    public Drawer() {
        myChat = new Chat();
        drawArea = new DrawArea();
        initUI();
        messageObservable.addObserver(this);
    }

    private void initUI() {
        setSize(400, 500);
        setLayout(null);
        drawArea.setBounds(0, 0, drawArea.getWidth(), drawArea.getHeight());
        myChat.setBounds(0, getHeight() - myChat.getHeight(), myChat.getWidth(), myChat.getHeight());
        add(myChat);
        add(drawArea);
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        EventMessage message = (EventMessage) arg;
        switch(message.getEventType()) {
            case MESSAGE:
                String body = message.getData().get("message"),
                        fontSize = message.getData().get("fontSize"),
                        color = message.getData().get("color"),
                        sender = message.getData().get("sender"),
                        group = message.getData().get("group");
                Date sentDate = new Date(Long.parseLong(message.getData().get("date")));
                //This adds text to UI when delivered to server it's like a confirmation
                if(group.equals(Session.getViewingGroup())) {
                    myChat.addText(sender, sentDate, body, fontSize, color);
                }
                break;
            case DRAWING:
                int x1 = Integer.parseInt(message.getData().get("x1")),
                        x2 = Integer.parseInt(message.getData().get("x2")),
                        y1 = Integer.parseInt(message.getData().get("y1")),
                        y2 = Integer.parseInt(message.getData().get("y2"));

                ShapeSelection shape = ShapeSelection.fromString(message.getData().get("shape"));
                sender = message.getData().get("sender");
                group = message.getData().get("group");
                if(!sender.equals(Session.getCurrentUser()) && group.equals(Session.getViewingGroup())) {
                    DrawingModel drawingModel = new DrawingModel();
                    drawingModel.setX1(x1);
                    drawingModel.setX2(x2);
                    drawingModel.setY1(y1);
                    drawingModel.setY2(y2);
                    drawingModel.setSender(message.getData().get("sender"));
                    drawingModel.setShapeType(shape);
                    drawingModel.setColor(message.getData().get("color"));
                    drawArea.setRealTimeDrawing(drawingModel);
                }
                break;
            case DREW:
                x1 = Integer.parseInt(message.getData().get("x1"));
                x2 = Integer.parseInt(message.getData().get("x2"));
                y1 = Integer.parseInt(message.getData().get("y1"));
                y2 = Integer.parseInt(message.getData().get("y2"));

                String drawingColor = message.getData().get("color");
                shape = ShapeSelection.fromString(message.getData().get("shape"));
                sender = message.getData().get("sender");
                group = message.getData().get("group");
                if(!sender.equals(Session.getCurrentUser()) && group.equals(Session.getViewingGroup())) {
                    drawArea.finaliseTemporaryDrawing(sender);
                    drawArea.drawShape(x1, x2, y1, y2, shape, drawingColor);
                }
                break;
            case CHANGE_CONTEXT:
                DashBoardGetter dashBoardGetter = new DashBoardGetter();
                dashBoardGetter.execute();
                break;
            case ADD_USER:
                group = message.getData().get("group");
                if(group.equals(Session.getViewingGroup())) {
                    String user = message.getData().get("user");
                    drawArea.addLegend(user);
                }
                break;
            case SAVE_IMAGE:
                try {
                    drawArea.save();
                } catch (IOException e) {
                    LOG.error("Failed to save image ", e);
                }
            default:
                break;
        }
    }

    private class DashBoardGetter extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return dashBoardService.getDashBoard(Session.getViewingGroup());
        }

        @Override
        protected void done() {
            try {
                HTTPResponse response = get();
                DashBoardModel dashBoardModel = objectMapper.readValue(response.getBody(), DashBoardModel.class);
                myChat.runInContext(dashBoardModel.getMessagesModel());
                drawArea.runInContext(dashBoardModel.getDrawingsModel());
            } catch (Exception ex) {
                LOG.error("Failure at running in context ", ex);
            }
        }
    }
}
