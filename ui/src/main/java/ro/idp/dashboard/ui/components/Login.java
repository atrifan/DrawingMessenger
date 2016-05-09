package ro.idp.dashboard.ui.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.atrifan.client.http.util.HTTPResponse;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.model.LoginModel;
import ro.atrifan.model.UserModel;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.model.queue.Events;
import ro.idp.dashboard.connector.http.UserService;
import org.apache.log4j.Logger;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.Session;
import ro.idp.dashboard.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Enti on 2/23/2016.
 */
public class Login extends JPanel implements ActionListener{

    private JLabel labelUsername = new JLabel("Enter username: ");
    private JLabel labelPassword = new JLabel("Enter password: ");
    private JTextField textUsername = new JTextField();
    private JPasswordField fieldPassword = new JPasswordField();
    private JButton buttonLogin = new JButton("Login");
    private UserService userService = UserService.getInstance();
    private JLabel scenetitle = new JLabel("Login");
    final JLabel actiontarget = new JLabel();
    private Logger LOG = Logger.getLogger(Login.class);
    private ObjectMapper objectMapper = new ObjectMapper();


    public Login() {
        initUI();
        initActions();
    }

    private void initActions() {
        buttonLogin.addActionListener(this);
    }

    private CompoundBorder getBlackBorder() {
        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        Border empty = new EmptyBorder(0, 9, 0, 0);
        CompoundBorder border = new CompoundBorder(line, empty);
        return border;
    }

    private CompoundBorder getRedBorder() {
        Border line = BorderFactory.createLineBorder(Color.RED);
        Border empty = new EmptyBorder(0, 9, 0, 0);
        CompoundBorder border = new CompoundBorder(line, empty);
        return border;
    }

    public void initUI() {
        setLayout(null);
        add(scenetitle);
        scenetitle.setFont(new Font("Tahoma", Font.BOLD, 20));

        /**POSITIONS**/
        setSize(360, 240);
        scenetitle.setBounds(10, 20, 120, 30);
        labelUsername.setBounds(10, 60, 120, 20);
        textUsername.setBounds(150, 60, 200, 20);
        textUsername.setBorder(getBlackBorder());
        labelPassword.setBounds(10, 90, 120, 20);
        fieldPassword.setBounds(150, 90, 200, 20);
        fieldPassword.setBorder(getBlackBorder());
        buttonLogin.setBounds(280, 140, 70, 20);
        actiontarget.setBounds(10, 170, 400, 20);
        actiontarget.setVisible(false);
        actiontarget.setForeground(Color.RED);

        add(labelUsername);
        add(textUsername);
        add(labelPassword);
        add(fieldPassword);
        add(buttonLogin);
        add(actiontarget);
        setVisible(true);
    }

    protected boolean isFormValid() {
        String userName = textUsername.getText(),
                password = fieldPassword.getText();

        boolean valid = true;
        if(userName.isEmpty()) {
            valid = false;
            textUsername.setBorder(getRedBorder());
        } else {
            textUsername.setBorder(getBlackBorder());
        }

        if(password.isEmpty()) {
            valid = false;
            fieldPassword.setBorder(getRedBorder());
        } else {
            fieldPassword.setBorder(getBlackBorder());
        }

        return valid;
    }


    private void login(String userName, String password) {
        AbstractSwingWorker worker = new AbstractSwingWorker(userName, password);
        try {
            worker.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorToClient(String message) {
        actiontarget.setText(message);
        actiontarget.setVisible(true);
    }

    private CallBack loginCallBackHandler(final String userName, final String password) {
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
                    try {
                        UserModel userModel = objectMapper.readValue(response.getBody(), UserModel.class);
                        LOG.info(String.format("Logged in succesfully <{%s}>", userName));
                        Session.setCurrentUser(userModel.getUserName());
                        Session.setUserColorName(userModel.getColor());
                        LOG.info(String.format("Saved data to session"));
                        EventMessage message = new EventMessage();
                        message.setEventType(Events.LOGIN);
                        message.setData("userName", userModel.getUserName());
                        message.setData("color", userModel.getColor());
                        MessageObservable.getInstance().changeData(message);
                    } catch (IOException e) {
                        LOG.error("Failed to decrypt response message", e);
                    }
                    break;
                case INTERNAL_SERVER_ERROR:
                    LOG.info(String.format("Failed to log in <{%s}>", userName));
                    showErrorToClient(String.format("Failed to log in <{%s}>", userName));
                    break;
                case FOUND:
                    LOG.info(String.format("User <{%s}> is already logged in you cannot log in", userName));
                    showErrorToClient(String.format("User <{%s}> is already logged in you cannot log in", userName));
                    break;
                case UNAUTHORIZED:
                    LOG.info(String.format("User <{%s}> correct but password is wrong", userName));
                    showErrorToClient(String.format("User <{%s}> correct but password is wrong", userName));
                    break;
                default:
                    LOG.info(String.format("Unknow status code <{%s}> received at login for user <{%s}>", userName,
                            statusCode.getStatusCode()));
                    showErrorToClient(String.format("Unknow status code <{%s}> received at login for user <{%s}>", userName,
                            statusCode.getStatusCode()));
                    break;
                }
            }
        };
        return callBackHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(isFormValid()) {
            actiontarget.setVisible(false);
            String userName = textUsername.getText(),
                    passWord = fieldPassword.getText();
            login(userName, passWord);
        } else {
            showErrorToClient("Please fill all form data");
            actiontarget.setVisible(true);
        }
    }


    class AbstractSwingWorker extends SwingWorker<HTTPResponse, Void> {

        private String userName;
        private String password;

        public AbstractSwingWorker(String userName, String password) {
            super();
            this.userName = userName;
            this.password = password;

        }
        @Override
        protected HTTPResponse doInBackground() throws Exception {
            LoginModel loginModel = new LoginModel();
            loginModel.setPassword(password);
            loginModel.setUserName(userName);
            loginModel.setColor(Util.getRandomColor());
            return userService.login(loginModel, loginCallBackHandler(userName, password));
        }

        protected void done() {
            HTTPResponse response = null;
            try {
                response = get();
                Response.Status statusCode = Response.Status.fromStatusCode(response.getCode());
                switch (statusCode) {
                    case OK:
                        try {
                            UserModel userModel = objectMapper.readValue(response.getBody(), UserModel.class);
                            LOG.info(String.format("Logged in succesfully <{%s}>", userName));
                            Session.setCurrentUser(userModel.getUserName());
                            Session.setUserColorName(userModel.getColor());
                            LOG.info(String.format("Saved data to session"));
                            EventMessage message = new EventMessage();
                            message.setEventType(Events.LOGIN);
                            message.setData("userName", userModel.getUserName());
                            message.setData("color", userModel.getColor());
                            MessageObservable.getInstance().changeData(message);
                        } catch (IOException e) {
                            LOG.error("Failed to decrypt response message", e);
                        }
                        break;
                    case INTERNAL_SERVER_ERROR:
                        LOG.info(String.format("Failed to log in <{%s}>", userName));
                        showErrorToClient(String.format("Failed to log in <{%s}>", userName));
                        break;
                    case FOUND:
                        LOG.info(String.format("User <{%s}> is already logged in you cannot log in", userName));
                        showErrorToClient(String.format("User <{%s}> is already logged in you cannot log in", userName));
                        break;
                    case UNAUTHORIZED:
                        LOG.info(String.format("User <{%s}> correct but password is wrong", userName));
                        showErrorToClient(String.format("User <{%s}> correct but password is wrong", userName));
                        break;
                    default:
                        LOG.info(String.format("Unknow status code <{%s}> received at login for user <{%s}>", userName,
                                statusCode.getStatusCode()));
                        showErrorToClient(String.format("Unknow status code <{%s}> received at login for user <{%s}>", userName,
                                statusCode.getStatusCode()));
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }
}
