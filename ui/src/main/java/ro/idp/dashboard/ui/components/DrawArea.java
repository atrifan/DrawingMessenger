package ro.idp.dashboard.ui.components;

import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.model.*;
import ro.atrifan.persistence.entities.User;
import ro.atrifan.util.Logger;
import ro.idp.dashboard.connector.http.GroupService;
import ro.idp.dashboard.connector.http.UserService;
import ro.idp.dashboard.util.Session;
import ro.idp.dashboard.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created by Enti on 2/24/2016.
 */
public class DrawArea extends JPanel{
    private JButton square = new JButton("[]"),
            circle = new JButton("()"),
            arrow = new JButton("->"),
            line = new JButton("/");


    private JScrollPane scrollLegend = new JScrollPane();
    private JPanel legend = new JPanel();
    PaintSurface drawArea = new PaintSurface();
    ObjectMapper objectMapper = new ObjectMapper();
    private GroupService groupService = GroupService.getInstance();
    Logger LOG = Logger.getLogger(DrawArea.class);
    private UserService userService = UserService.getInstance();

    public DrawArea() {
        initUI();
        initDrawingButtons();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }


    private void initUI() {
        setVisible(false);
        setSize(400, 230);
        setLayout(null);
        setDrawingButtonsPosition();
        add(square);
        add(circle);
        add(arrow);
        add(line);

        setDrawingArea();
        add(drawArea);

        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        scrollLegend = new JScrollPane(legend);
        setLegend();
        add(scrollLegend);
        setVisible(true);
    }

    private void setDrawingArea() {
        drawArea.setBounds(0, 30, 300, 200);
        drawArea.setBackground(Color.WHITE);
    }

    private void getLegend() {
        JLabel legendLabel = new JLabel("Legend");
        legend.add(legendLabel);
        LegendWorker legendWorker = new LegendWorker();
        legendWorker.execute();
    }


    private class LegendWorker extends SwingWorker<HTTPResponse, Void> {

        @Override
        protected HTTPResponse doInBackground() throws Exception {
            LOG.info(String.format("Getting legend for group <%s>", Session.getViewingGroup()));
            return groupService.getUsersOfGroup(Session.getViewingGroup(), null);
        }

        protected void done() {
            try {
                HTTPResponse response = get();
                UsersModel usersModel = objectMapper.readValue(response.getBody(), UsersModel.class);
                for(UserModel userModel : usersModel.getUsers()) {
                    addEntryToLegend(userModel.getUserName(), userModel.getColor());
                }
            } catch (Exception ex) {
                LOG.error(String.format("Failed to get users of group <%s>", Session.getViewingGroup()), ex);
            }
        }
    }

    private void setLegend() {
        scrollLegend.setBounds(310, 30, 90, 200);
    }

    private void addEntryToLegend(String userName, String color) {
        JLabel entry = new JLabel(userName);
        entry.setForeground(Util.getColorByString(color));
        legend.add(entry);
        legend.revalidate();
        legend.repaint();
    }



    private void setDrawingButtonsPosition() {
        square.setBounds(0, 0, 60, 20);
        circle.setBounds(70, 0, 60, 20);
        arrow.setBounds(140, 0, 60, 20);
        line.setBounds(210, 0, 60, 20);
    }

    public void initDrawingButtons() {
        square.setActionCommand("rectangle");
        circle.setActionCommand("circle");
        arrow.setActionCommand("arrow");
        line.setActionCommand("line");
        square.addActionListener(setDrawingType());
        circle.addActionListener(setDrawingType());
        arrow.addActionListener(setDrawingType());
        line.addActionListener(setDrawingType());
    }

    private void enableButtons() {
        square.setEnabled(true);
        circle.setEnabled(true);
        arrow.setEnabled(true);
        line.setEnabled(true);
    }
    private ActionListener setDrawingType() {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableButtons();
                JButton presedButton = (JButton) e.getSource();
                presedButton.setEnabled(false);
                drawArea.setShape(ShapeSelection.fromString(e.getActionCommand()));
            }
        };
        return actionListener;
    }

    public void drawShape(int x1, int x2, int y1, int y2, ShapeSelection shape, String color) {
        drawArea.drawShape(x1, x2, y1, y2, shape, color);
    }

    public void setRealTimeDrawing(DrawingModel drawingModel) {
        drawArea.setTemporarryToDraw(drawingModel);
        drawArea.repaint();
    }

    public void runInContext(DrawingsModel drawingsModel) {
        drawArea.runInContext(drawingsModel);
        drawArea.repaint();
        legend.removeAll();
        legend.revalidate();
        legend.repaint();
        getLegend();
    }

    public void addLegend(String userName) {
        UserGetterWorker userGetterWorker = new UserGetterWorker(userName);
        userGetterWorker.execute();
    }

    public void save() throws IOException {
        drawArea.save();
    }
    private class UserGetterWorker extends SwingWorker<HTTPResponse, Void> {

        private String userName;
        public UserGetterWorker(String userName) {
            this.userName = userName;
        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return userService.getUser(userName);
        }

        @Override
        protected void done() {
            try {
                HTTPResponse response = get();
                UserModel userModel = objectMapper.readValue(response.getBody(), UserModel.class);
                addEntryToLegend(userModel.getUserName(), userModel.getColor());
            } catch (Exception ex) {
                LOG.error(String.format("Failed to get user info for <%s>", userName), ex);
            }
        }
    }
    public void finaliseTemporaryDrawing(String sender) {
        drawArea.finaliseTemporaryDrawing(sender);
    }

}
