package ro.idp.dashboard.connector.http;

import ro.atrifan.client.http.util.HTTPMethod;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.client.worker.NIOWorker;

/**
 * Created by alexandru.trifan on 29.04.2016.
 */
public class HttpWorkRequest implements Runnable {

    HTTPMethod method;
    long timeout;
    CallBack callBack;
    public HttpWorkRequest(HTTPMethod method, long timeout, CallBack callBack) {
        this.method = method;
        this.timeout = timeout;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        NIOWorker nioWorkerSingleton = NioWorkerSingleton.getNioWorker();
        nioWorkerSingleton.execute(method, timeout, callBack);
    }
}
