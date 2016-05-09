package ro.idp.dashboard.connector.queue;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.model.queue.EventMessage;
import ro.idp.dashboard.ui.components.MessageObservable;
import ro.idp.dashboard.util.MessageQueue;

import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Created by alexandru.trifan on 02.05.2016.
 */
public class MessageWorker implements Runnable {

    private Logger LOG = Logger.getLogger(MessageWorker.class);
    MessageObservable messageObservable = MessageObservable.getInstance();

    private Message message;
    public MessageWorker(Message message) {
        this.message = message;
    }


    public void run() {
        try {
            if(message instanceof TextMessage) {
                String body = ((TextMessage) message).getText();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    EventMessage convertedMessage = mapper.readValue(body, EventMessage.class);
                    LOG.info(String.format("Consumed message <{%s}> from queue, treating it",
                            body));
                    treatMessage(convertedMessage);
                } catch (Exception ex) {
                    LOG.error(String.format("Failed to read message <{%s}> from queue",
                            body), ex);
                    //I am putting it back here if it were a queue
                    throw ex;
                }
            }
            LOG.info("Message consumption acknowledged");
            message.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void treatMessage(EventMessage message) {
        LOG.info("Publishing message to QueueMessage");
        messageObservable.changeData(message);
    }
}
