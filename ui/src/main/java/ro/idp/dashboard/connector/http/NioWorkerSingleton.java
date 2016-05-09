package ro.idp.dashboard.connector.http;

import ro.atrifan.client.worker.NIOWorker;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by atrifan on 4/25/2016.
 */
public class NioWorkerSingleton {

    private static NIOWorker nioWorker = null;
    private static Logger LOG = Logger.getLogger(NioWorkerSingleton.class);
    public synchronized static NIOWorker getNioWorker() {
        LOG.info("Getting NIOWorkerInstance");
        if(nioWorker == null) {
            try {
                nioWorker = new NIOWorker();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return nioWorker;
    }
}
