package ro.idp.dashboard.util;

import org.codehaus.jackson.map.ObjectMapper;
import ro.atrifan.model.queue.EventMessage;
import ro.idp.dashboard.connector.queue.MessageWorker;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class MessageQueue {

    private final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MessageQueue.class);
    ObjectMapper objectMapper = new ObjectMapper();
    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(20,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            });

    private class Publisher implements Runnable {

        EventMessage eventMessage;
        public Publisher(EventMessage eventMessage) {
            this.eventMessage = eventMessage;
        }

        @Override
        public void run() {
            for(RealWorker realWorker : subscribers) {
                realWorker.addTask(eventMessage);
            }
        }
    }

    private class SubscriberAdder implements Runnable {

        RealWorker realWorker;
        public SubscriberAdder(RealWorker realWorker) {
            this.realWorker = realWorker;
        }

        public void run() {
            subscribers.add(realWorker);
            realWorker.start();
        }
    }

    private class RealWorker extends Thread {

        //TODO this is butleNeck should rethink
        private MessageQueueWorker queueHandler;
        private Vector<EventMessage> internalQueue = new Vector<>();
        private boolean destroyed = false;

        public RealWorker(MessageQueueWorker queueHandler) {
            this.queueHandler = queueHandler;
        }

        public long getId() {
            return queueHandler.id;
        }

        public void addTask(EventMessage message) {
            if(destroyed) {
                return;
            }
            //boolean isAdded = internalQueue.offer(message);
            internalQueue.add(message);
        }

        @Override
        public void run() {
            while(!destroyed) {
                if(internalQueue.size() > 0) {
                    EventMessage event = internalQueue.get(0);
                    internalQueue.remove(0);
                    queueHandler.handleMessage(event);
                }
            }
        }
    }
    private static MessageQueue instance = null;

    public static MessageQueue getInstance() {
        if(instance == null) {
            instance = new MessageQueue();
        }

        return instance;
    }


    private MessageQueue(){}

    private Vector<RealWorker> subscribers = new Vector<>();
    public void subscribeToQueue(MessageQueueWorker worker) {
        LOG.info(String.format("Subscribed to internal queue <{%s}>", worker.id));
        RealWorker thread = new RealWorker(worker);
        threadPoolExecutor.submit(new SubscriberAdder(thread));
    }


    public void publishMessage(EventMessage message) {
        try {
            LOG.info(String.format("Publishing message to internal queue <%s>", objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            LOG.warn("The message published to internal queue is not json", e);
        }
        threadPoolExecutor.submit(new Publisher(message));
    }

    public void unSubscribe(long id) throws InterruptedException {
        for (int i = 0; i < subscribers.size(); i++) {
            RealWorker subscriber = subscribers.get(i);
            if (subscriber.getId() == id) {
                subscribers.remove(i);
                subscriber.destroyed = true;
                break;
            }
        }
    }
}
