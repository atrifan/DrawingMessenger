package ro.atrifan.util.queue;

import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.model.queue.EventMessage;
import org.apache.log4j.Logger;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.concurrent.*;

/**
 * Created by atrifan on 1/29/2016.
 */
public abstract class MessageHandler implements MessageListener {

    protected ExecutorService threadPoolExecutor;

    public MessageHandler() {
        super();
        threadPoolExecutor = Executors.newFixedThreadPool(20,
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    }
                });
    }

    public abstract void onMessage(Message message);
}
