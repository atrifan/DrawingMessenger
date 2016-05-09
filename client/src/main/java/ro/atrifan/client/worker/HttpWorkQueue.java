package ro.atrifan.client.worker;

import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.HTTPMethod;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by alexandru.trifan on 02.05.2016.
 */
public class HttpWorkQueue {
    private ExecutorService threadPoolExecutor = null;
    private static HttpWorkQueue instance = null;
    private Logger LOG = Logger.getLogger(HttpWorkQueue.class);

    public static HttpWorkQueue getInstance() {
        if (instance == null) {
            instance = new HttpWorkQueue();
        }
        return instance;
    }

    public HttpWorkQueue() {
        threadPoolExecutor = Executors.newFixedThreadPool(30,
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    }
                });
    }


    public void addWork(HTTPMethod method, long timeout, CallBack callBack) throws IOException {
        LOG.info("Adding work");
        threadPoolExecutor.execute(new NIOTask(method, timeout, callBack));
    }
}
