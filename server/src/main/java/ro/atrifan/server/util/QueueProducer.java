package ro.atrifan.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import ro.atrifan.util.queue.AMQConnection;
import javax.jms.*;
import java.io.IOException;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class QueueProducer {

    private static final Logger LOG = Logger.getLogger(QueueProducer.class);
    private static QueueProducer instance = null;
    private ObjectMapper objectMapper = new ObjectMapper();

    public static QueueProducer getProducer() throws IOException, JMSException {
        if(instance != null) {
            return instance;
        }

        PropertiesReader propertiesReader = PropertiesReader.load();
        Session session = AMQConnection.getSessionInstance(propertiesReader.getMessageQueuHost());
        try {
            ActiveMQTopic communicationTopic = (ActiveMQTopic) session.createTopic(propertiesReader.getMessageQueueQueueName());
            MessageProducer producer = session.createProducer(communicationTopic);
            instance = new QueueProducer(producer, session);
            LOG.info("Initialized queue producer successfully");
        } catch (JMSException e) {
            LOG.error("Failed to initialize queue producer ", e);
            throw e;
        }

        return instance;
    }

    private MessageProducer messageProducer;
    private Session session;
    private QueueProducer(MessageProducer producer, Session session) {
        this.messageProducer = producer;
        this.session = session;
    }

    public void produce(Object message) throws JMSException, JsonProcessingException {
        TextMessage messageToSend = session.createTextMessage(objectMapper.writeValueAsString(message));
        messageProducer.send(messageToSend);
    }

}
