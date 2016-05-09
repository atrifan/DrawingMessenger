package ro.atrifan.util.queue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;

public class AMQConnection {

    private static class Loader {
        ActiveMQConnection connection = null;
        private Session session = null;

        public Loader(String url) {
            this.connection = createActiveMQConnection(url);
            this.session = createSession();
        }

        private ActiveMQConnection createActiveMQConnection(String url) {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            ActiveMQConnection conn = null;

            try {
                conn = (ActiveMQConnection) connectionFactory.createConnection();
                conn.start();
            } catch (JMSException e) {
                e.printStackTrace();
            } finally {
                return conn;
            }
        }

        private Session createSession() {
            Session single_session = null;
            try {
                single_session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                e.printStackTrace();
            } finally {
                return single_session;
            }
        }
    }

    private static HashMap<String, Loader> instance = new HashMap<String, Loader>();

    public static ActiveMQConnection getConnectionInstance(String amqHost) {
        return instance.get(amqHost).connection;
    }

    public static Session getSessionInstance(String amqHost) {
        if(instance.containsKey(amqHost)) {
            return instance.get(amqHost).session;
        } else {
            Loader loaderInstance = new Loader(amqHost);
            instance.put(amqHost, loaderInstance);
            return loaderInstance.session;
        }
    }

    public static void closeSession(String amqHost) throws JMSException {
        instance.get(amqHost).session.close();
    }

    public static void closeConnection(String amqHost) throws JMSException {
        instance.get(amqHost).connection.close();
    }

    /*public static void main(String[] args) {
        final String destinationName = "ssl.monitoring";
        Session session = AMQConsumer.getSessionInstance();
        try {
            ActiveMQQueue ssl_monitoring_queue = (ActiveMQQueue) session.createQueue(destinationName);
            MessageConsumer consumer = session.createConsumer(ssl_monitoring_queue);
            consumer.setMessageListener((MessageListener) new MonitoringMessageHandler());
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }*/
}
