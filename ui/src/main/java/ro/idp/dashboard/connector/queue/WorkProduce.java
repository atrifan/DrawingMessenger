package ro.idp.dashboard.connector.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.jms.MessageProducer;
import javax.jms.*;

/**
 * Created by alexandru.trifan on 02.05.2016.
 */
public class WorkProduce implements Runnable {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Session session;
    private MessageProducer messageProducer;
    private Object message;
    private Logger LOG = Logger.getLogger(WorkProduce.class);
    public WorkProduce(Object message, MessageProducer messageProducer, Session session) {
        this.session = session;
        this.messageProducer = messageProducer;
        this.message = message;

    }

    public void run() {
        TextMessage messageToSend = null;
        try {
            messageToSend = session.createTextMessage(objectMapper.writeValueAsString(message));
            messageProducer.send(messageToSend);
        } catch (JMSException | JsonProcessingException e) {
            LOG.error("Failed to produce work to queue", e);
        }
    }
}
