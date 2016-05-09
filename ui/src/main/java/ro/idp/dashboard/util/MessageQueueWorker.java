package ro.idp.dashboard.util;


import ro.atrifan.model.queue.EventMessage;

import java.util.Date;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public interface MessageQueueWorker{
    long id = new Date().getTime();
    void handleMessage(EventMessage message);
}
