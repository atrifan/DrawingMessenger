package ro.idp.dashboard.ui.components;

import ro.atrifan.model.queue.EventMessage;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Map;
import java.util.Observer;

/**
 * Created by Enti on 2/23/2016.
 */
public class App extends JFrame implements Observer {

    private Logger LOG = Logger.getLogger(App.class);
    MainApp mainApp;
    MessageObservable messageObservable = MessageObservable.getInstance();

    public void start() {
        messageObservable.addObserver(this);
        initLoginUI();
    }

    private void initLoginUI() {
        Login loginView = new Login();
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginView.setAlignmentX(400 - loginView.getHeight() / 2);
        loginView.setAlignmentY(300 - loginView.getWidth() / 2);
        getContentPane().setLayout(null);
        getContentPane().add(loginView);
        setTitle("DashBoard");
        setVisible(true);
        int panelX = (getWidth() - loginView.getWidth() - getInsets().left - getInsets().right) / 2;
        int panelY = ((getHeight() - loginView.getHeight() - getInsets().top - getInsets().bottom) / 2);
        loginView.setLocation(panelX, panelY);

    }

    private void refresh() {
        invalidate();
        validate();
        repaint();
    }

    public void initAppUI(Map<String, String> message) {
        getContentPane().remove(0);
        mainApp = new MainApp(message);
        getContentPane().add(mainApp);
        refresh();
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        EventMessage message = (EventMessage) arg;
        LOG.info("Treating message");
        switch (message.getEventType()) {
            case LOGIN :
                LOG.info("Entering applications");
                initAppUI(message.getData());
                break;
            case LOGOUT:
                System.exit(0);
                LOG.info("Leaving application");
            default:
                break;
        }
    }
}

