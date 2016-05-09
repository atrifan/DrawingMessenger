package ro.idp.dashboard.connector.queue;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import ro.atrifan.model.queue.EventMessage;
import ro.atrifan.util.queue.AMQConnection;
import ro.atrifan.util.queue.MessageHandler;
import ro.idp.dashboard.util.MessageQueue;
import ro.idp.dashboard.util.PropertiesReader;

import javax.jms.*;
import java.io.IOException;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class QueueConsumer {


    private static boolean initialized = false;
    private static final Logger LOG = Logger.getLogger(QueueConsumer.class);
    private static class MessageHandlerImpl extends MessageHandler {

        @Override
        public void onMessage(Message message) {
            LOG.info("Received message from AMQ");
            super.threadPoolExecutor.execute(new MessageWorker(message));
        }
    }

    public static void initConsumer() throws IOException {
        if(initialized) {
            LOG.warn("Trying to initialize consumer again -- this is not allowed, you can initiate queue consumer " +
                    "only once in your app");
            return;
        }

        PropertiesReader propertiesReader = PropertiesReader.load();
        Session session = AMQConnection.getSessionInstance(propertiesReader.getMessageQueuHost());
        try {
            ActiveMQTopic communicationQueue = (ActiveMQTopic) session.createTopic(propertiesReader.getMessageQueueQueueName());
            MessageConsumer consumer = session.createConsumer(communicationQueue);
            consumer.setMessageListener((MessageListener) new MessageHandlerImpl());
            initialized = true;
            LOG.info("Initialized queue consumer successfully");
        } catch (JMSException e) {
            LOG.error("Failed to initialize queue consumer ", e);
        }

        return;
    }

}
