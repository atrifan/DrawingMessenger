package ro.idp.dashboard.connector.http;

import ro.atrifan.client.http.util.HTTPMethod;
import ro.atrifan.client.worker.CallBack;

import java.util.concurrent.*;

/**
 * Created by alexandru.trifan on 29.04.2016.
 */
public class HttpWorkPool {

    ExecutorService threadPoolExecutor;

    private static HttpWorkPool instance = null;
    public static HttpWorkPool getInstance() {
        if(instance == null) {
            instance = new HttpWorkPool();
        }

        return instance;
    }

    private HttpWorkPool() {
         threadPoolExecutor = Executors.newFixedThreadPool(20,
                 new ThreadFactory() {
                     public Thread newThread(Runnable r) {
                         Thread t = Executors.defaultThreadFactory().newThread(r);
                         t.setDaemon(true);
                         return t;
                     }
                 });
    }

    public void addWork(HTTPMethod method, long timeout, CallBack callBack) {
        threadPoolExecutor.submit(new HttpWorkRequest(method, timeout, callBack));
    }
}
