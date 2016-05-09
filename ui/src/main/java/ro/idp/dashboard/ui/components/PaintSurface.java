package ro.idp.dashboard.ui.components;

import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.model.DrawingModel;
import ro.atrifan.model.DrawingsModel;
import ro.atrifan.model.ShapeSelection;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.DashBoardService;
import ro.idp.dashboard.connector.queue.QueueProducer;
import ro.idp.dashboard.util.Session;
import ro.idp.dashboard.util.Util;

import javax.imageio.ImageIO;
import javax.jms.JMSException;
import javax.swing.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/27/2016.
 */
public class PaintSurface extends JComponent{
    List<DrawingModel> shapes = new ArrayList<>();
    List<Color> colors = new ArrayList<>();
    private BufferedImage paintImage = new BufferedImage(500, 400, BufferedImage.TYPE_3BYTE_BGR);

    Point startDrag, endDrag;
    Date startingTime;
    ShapeSelection shape = null;
    private DashBoardService dashBoardService = DashBoardService.getInstance();
    QueueProducer queueProducer;
    Logger LOG = Logger.getLogger(PaintSurface.class);
    Vector<DrawingModel> currentDrawingModels = new Vector<>();

    public PaintSurface() {
        try {
            queueProducer = QueueProducer.getProducer();
        } catch (IOException | JMSException e) {
            LOG.error("Failed to get amq producer", e);
        }

        setBackground(Color.WHITE);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(shape == null) {
                    return;
                }
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;
                startingTime = new Date();
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                drawShape(e);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (shape == null) {
                    return;
                }
                endDrag = new Point(e.getX(), e.getY());
                repaint();
            }
        });
    }

    public void drawShape(int x1, int x2, int y1, int y2, ShapeSelection shape, String color) {
        DrawingModel drawingModel = new DrawingModel();
        drawingModel.setX1(x1);
        drawingModel.setX2(x2);
        drawingModel.setY1(y1);
        drawingModel.setY2(y2);
        drawingModel.setColor(color);
        drawingModel.setShapeType(shape);
        shapes.add(drawingModel);

        repaint();
    }

    private void drawShape(MouseEvent e) {
        if(shape == null) {
            return;
        }

        int finalX = e.getX(),
                finalY = e.getY();

        drawShape(startDrag.x, finalX, startDrag.y, finalY, this.shape, Session.getUserColorName());
        DrawingModel drawingModel = new DrawingModel();
        drawingModel.setColor(Session.getUserColorName());
        drawingModel.setSender(Session.getCurrentUser());
        drawingModel.setShapeType(shape);
        drawingModel.setX1(startDrag.x);
        drawingModel.setX2(finalX);
        drawingModel.setY1(startDrag.y);
        drawingModel.setY2(finalY);

        startDrag = null;
        endDrag = null;
        DrawingWorker drawingWorker = new DrawingWorker(drawingModel);
        drawingWorker.execute();
    }

    private class DrawingWorker extends SwingWorker<HTTPResponse, Void> {

        private DrawingModel drawingModel;
        public DrawingWorker(DrawingModel drawingModel) {
            this.drawingModel = drawingModel;
        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            return dashBoardService.sendDrawing(Session.getViewingGroup(), drawingModel, null);
        }

        @Override
        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info("Successfully sent drawing to server");
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.warn("Failed to send drawing to server");
                        break;
                    default:
                        LOG.warn(String.format("Unknow status code <{%s}> received, most likely drawing failed" +
                                " to send to server", statusCode));
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    public void drawShapeTemporary(Graphics2D g2, int x1, int x2, int y1, int y2, ShapeSelection shape, Color color) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
        Object s = makeShape(x1, y1, x2, y2, shape);
        if(s instanceof Shape) {
            g2.draw((Shape)s);
            g2.setPaint(color);
            g2.fill((Shape)s);
        } else {
            for(Shape r : (ArrayList<Shape>)s) {
                g2.draw(r);
            }
        }
    }

    public void setShape(ShapeSelection shape) {
        this.shape = shape;
    }

    private void paintBackground(Graphics2D g2){
        Shape background = makeRectangle(0, 0, getSize().width - 10, getSize().height - 10);
        g2.setPaint(Color.WHITE);
        g2.fill(background);
        /*for (int i = 0; i < getSize().width; i += 10) {
            Shape line = new Line2D.Float(i, 0, i, getSize().height);
            g2.draw(line);
        }

        for (int i = 0; i < getSize().height; i += 10) {
            Shape line = new Line2D.Float(0, i, getSize().width, i);
            g2.draw(line);
        }*/
    }
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintBackground(g2);

        g2.setStroke(new BasicStroke(2));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        for(int i = 0; i < shapes.size(); i++) {
            DrawingModel drawingModel = shapes.get(i);
            Object s = makeShape(drawingModel.getX1(),drawingModel.getY1(),drawingModel.getX2(), drawingModel.getY2(),
                    drawingModel.getShapeType());

            if(s instanceof Shape) {
                g2.setPaint(Util.getColorByString(drawingModel.getColor()));
                g2.draw((Shape) s);
                g2.setPaint(Util.getColorByString(drawingModel.getColor()));
                g2.fill((Shape) s);
            } else {
                for(Shape r : (ArrayList<Shape>)s) {
                    g2.setPaint(Util.getColorByString(drawingModel.getColor()));
                    g2.draw(r);
                    g2.setPaint(Util.getColorByString(drawingModel.getColor()));
                    g2.fill(r);
                }
            }
        }

        for(DrawingModel drawingModel : currentDrawingModels) {
            drawShapeTemporary(g2, drawingModel.getX1(), drawingModel.getX2(), drawingModel.getY1(),
                    drawingModel.getY2(), drawingModel.getShapeType(),
                    Util.getColorByString(drawingModel.getColor()));
        }

        if (startDrag != null && endDrag != null) {
            drawShapeTemporary(g2, startDrag.x, endDrag.x, startDrag.y, endDrag.y, this.shape, Session.getUserColor());

            //once every second send it
            EventMessage eventMessage = new EventMessage();
            eventMessage.setEventType(Events.DRAWING);
            eventMessage.setData("x1", startDrag.x + "");
            eventMessage.setData("y1", startDrag.y + "");
            eventMessage.setData("x2", endDrag.x + "");
            eventMessage.setData("y2", endDrag.y + "");
            eventMessage.setData("color", Session.getUserColorName());
            eventMessage.setData("sender", Session.getCurrentUser());
            eventMessage.setData("shape", shape.getText());
            eventMessage.setData("date", new Date().getTime() + "");
            eventMessage.setData("group", Session.getViewingGroup());
            QueueWorker queueWorker = new QueueWorker(eventMessage);
            queueWorker.execute();
        }

    }

    public void save() throws IOException{
        ImageIO.write(paintImage, "PNG", new File("drawing.png"));
        LOG.info("Saved drawing");
    }

    private class QueueWorker extends SwingWorker<Void, Void> {

        private EventMessage eventMessage;
        public QueueWorker(EventMessage eventMessage) {
            this.eventMessage = eventMessage;
        }
        @Override
        protected Void doInBackground() throws Exception {
            queueProducer.produce(eventMessage);
            return null;
        }
    }
    public void setTemporarryToDraw(DrawingModel drawingModel) {
        for(int i = 0; i < currentDrawingModels.size(); i++) {
            DrawingModel drawingModel1 = currentDrawingModels.get(i);
            if(drawingModel1.getSender().equals(drawingModel.getSender())) {
                currentDrawingModels.remove(i);
                break;
            }
        }

        currentDrawingModels.add(drawingModel);
    }

    public void finaliseTemporaryDrawing(String sender) {
        for(int i = 0; i < currentDrawingModels.size(); i++) {
            DrawingModel drawingModel1 = currentDrawingModels.get(i);
            if(drawingModel1.getSender().equals(sender)) {
                currentDrawingModels.remove(i);
                break;
            }
        }

    }

    private Object makeShape(int x1, int y1, int x2, int y2, ShapeSelection shape) {
        switch (shape) {
            case RECTANGLE:
                return makeRectangle(x1, y1, x2, y2);
            case CIRCLE:
                return makeCircle(x1, y1, x2, y2);
            case LINE:
                return makeLine(x1, y1, x2, y2);
            case ARROW:
                return makeArrow(x1, y1, x2, y2);
            default:
                return null;
        }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    private Ellipse2D.Float makeCircle(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    private Line2D.Float makeLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }

    private ArrayList<Shape> makeArrow(int x1, int y1, int x2, int y2) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        double rotation = 0f;
        rotation = -Math.atan2(deltaX, deltaY);
        rotation = Math.toDegrees(rotation) + 180;
        ArrowHead arrowHead = new ArrowHead();
        Rectangle bounds = arrowHead.getBounds();

        AffineTransform at = new AffineTransform();
        at.translate(x2  - (bounds.width / 2), y2 - (bounds.height / 2));
        at.rotate(Math.toRadians(rotation), bounds.width / 2, bounds.height / 2);
        Shape shape = new Path2D.Float(arrowHead, at);

        ArrayList<Shape> response = new ArrayList<>();
        response.add(makeLine(x1,y1,x2 ,y2));
        response.add(shape);
        return response;
    }

    public void runInContext(DrawingsModel drawingsModel) {
        shapes.clear();
        currentDrawingModels.clear();

        if(drawingsModel == null || drawingsModel.getDrawings() == null) {
            return;
        }

        for(DrawingModel drawingModel : drawingsModel.getDrawings()) {
            drawShape(drawingModel.getX1(), drawingModel.getX2(), drawingModel.getY1(), drawingModel.getY2(),
                    drawingModel.getShapeType(), drawingModel.getColor());
        }
    }

}

