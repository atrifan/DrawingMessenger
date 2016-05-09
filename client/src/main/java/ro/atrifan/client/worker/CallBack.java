package ro.atrifan.client.worker;

import ro.atrifan.client.http.util.HTTPResponse;

/**
 * Created by atrifan on 4/25/2016.
 */
public interface CallBack extends Runnable{

    public void setResponse(HTTPResponse response);
    public void run();
}
