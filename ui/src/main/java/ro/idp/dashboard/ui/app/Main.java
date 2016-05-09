package ro.idp.dashboard.ui.app;

import ro.atrifan.client.worker.NIOHttp;
import ro.idp.dashboard.connector.queue.QueueConsumer;
import ro.idp.dashboard.ui.components.App;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by Enti on 2/23/2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        QueueConsumer.initConsumer();
        NIOHttp nioHttp = NIOHttp.getInstance();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new App().start();
            }
        });
    }
}
