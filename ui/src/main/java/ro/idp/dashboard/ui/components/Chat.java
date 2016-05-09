package ro.idp.dashboard.ui.components;

import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.MessageModel;
import ro.atrifan.model.MessagesModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.DashBoardService;
import org.apache.log4j.Logger;
import ro.idp.dashboard.util.MessageQueueWorker;
import ro.idp.dashboard.util.Session;
import ro.idp.dashboard.util.Util;
import ro.idp.dashboard.util.MessageQueue;
import sun.reflect.generics.tree.VoidDescriptor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/23/2016.
 */
public class Chat extends JPanel implements ActionListener, KeyListener{
    private JLabel fontLabel = new JLabel("Font"),
        colorLabel = new JLabel("Color");

    private JComboBox font = new JComboBox(),
        color = new JComboBox();

    private JTextPane messages = new JTextPane();
    private JTextField messageWriter = new JTextField();
    private JScrollPane messagesScroll;
    private Logger LOG = Logger.getLogger(Chat.class);
    private DashBoardService dashBoardService = DashBoardService.getInstance();
    private JButton sendButton = new JButton("Send");

    public Chat() {
        initUI();
        initActions();
    }

    public void initUI() {
        setSize(400, 260);
        setLayout(null);
        messages.setBorder(setPadding(10));
        messages.setEditable(false);
        messageWriter.setBorder(setPadding(10));
        messagesScroll = new JScrollPane(messages);
        setPositionOfMessages();
        setPositionOfTextProperties();
        setPositionOfTextWritter();
        setPositionOfSendButton();
        setFontOptions();
        setColorOptions();
        add(messagesScroll);
        add(messageWriter);
        add(sendButton);
        add(font);
        add(fontLabel);
        add(color);
        add(colorLabel);
    }

    public void initActions() {
        System.out.println("OK");
        sendButton.addActionListener(this);
        messageWriter.addKeyListener(this);
    }

    private void setColorOptions() {

        List<String> colors = Util.getColorOptions();
        for(String colorField : colors) {
            color.addItem(colorField);
        }
    }


    private void setFontOptions() {
        String[] fonts = {"12", "14", "16", "18", "20", "22", "24"};
        for(int i = 0; i < fonts.length; i++) {
            font.addItem(fonts[i]);
        }
    }

    public CompoundBorder setPadding(int size) {
        Border line = BorderFactory.createLineBorder(Color.GRAY);
        Border empty = new EmptyBorder(0, size, 0, 0);
        CompoundBorder border = new CompoundBorder(line, empty);
        return border;
    }

    public void setPositionOfMessages() {
        messagesScroll.setBounds(0, 0, getWidth(), 180);
    }

    public void setPositionOfTextProperties() {
        fontLabel.setBounds(0, 190, 40, 20);
        font.setBounds(60, 190, 40, 20);
        colorLabel.setBounds(120, 190, 40, 20);
        color.setBounds(180, 190, 100, 20);
    }

    public void setPositionOfTextWritter() {
        messageWriter.setBounds(0, 230, 290, 20);
    }

    public void setPositionOfSendButton() {
        sendButton.setBounds(330, 230, 70, 20);
    }

    public void actionPerformed(ActionEvent e) {
        sendMessage();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER){
            sendMessage();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void sendMessage() {
        String message = messageWriter.getText(),
                fontSize = font.getSelectedItem().toString(),
                colorName = color.getSelectedItem().toString();


        AbstractWorker abstractWorker = new AbstractWorker(fontSize, colorName, message);
        abstractWorker.execute();
        messageWriter.setText("");
    }

    public CallBack sendMessageCallBackHandler() {
        CallBack callBackHandler = new CallBack() {
            private HTTPResponse response;

            @Override
            public void setResponse(HTTPResponse response) {
                this.response = response;
            }

            @Override
            public void run() {
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info("Message sent sucessfully");
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.warn("Failed to send message, message will not appear");
                        break;
                    default:
                        LOG.warn(String.format("Unknown statusCode <{%s}> received most likely message failed to send",
                                statusCode));
                        break;
                }
            }
        };
        return callBackHandler;
    }

    public void clearText() {
        messages.setText("");
    }

    public void addText(String sender, Date date, String text, String size, String color) {
        int fontSize = Integer.parseInt(size);
        Color selectedColor = Util.getColorByString(color);
        StyledDocument doc = messages.getStyledDocument();
        StyleContext context = new StyleContext();
        Style style = context.addStyle((new Date()).getTime() + "", null);
        StyleConstants.setForeground(style, selectedColor);
        StyleConstants.setFontSize(style, fontSize);
        try {
            doc.insertString(doc.getLength(), String.format("%s [%s]: %s\n", sender, date.toString(), text), style);
        } catch (BadLocationException e1) {
            LOG.error("Failed to parse message and add to textArea", e1);
        }

        refresh();
    }

    public void refresh() {
        invalidate();
        repaint();
        revalidate();
        setVisible(true);
    }


    private class AbstractWorker extends SwingWorker<HTTPResponse, Void> {

        private String fontSize,
                colorName,
                message;
        public AbstractWorker(String fontSize, String colorName, String message) {
            this.message = message;
            this.colorName = colorName;
            this.fontSize = fontSize;
        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            final MessageModel messageModel = new MessageModel();
            messageModel.setSender(Session.getCurrentUser());
            messageModel.setFontSize(fontSize);
            messageModel.setTextColor(colorName);
            messageModel.setMessage(message);
            return dashBoardService.sendMessage(Session.getViewingGroup(), messageModel, sendMessageCallBackHandler());
        }

        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        LOG.info("Message sent sucessfully");
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.warn("Failed to send message, message will not appear");
                        break;
                    default:
                        LOG.warn(String.format("Unknown statusCode <{%s}> received most likely message failed to send",
                                statusCode));
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void runInContext(MessagesModel messagesModel) {
        setVisible(false);
        clearText();

        if(messagesModel == null || messagesModel.getMessages() == null) {
            setVisible(true);
            return;
        }

        for(MessageModel messageModel : messagesModel.getMessages()) {
            addText(messageModel.getSender(), new Date(messageModel.getDate()), messageModel.getMessage(),
                    messageModel.getFontSize(), messageModel.getTextColor());
        }
        setVisible(true);
    }

}
