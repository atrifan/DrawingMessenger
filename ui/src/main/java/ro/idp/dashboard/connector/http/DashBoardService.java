package ro.idp.dashboard.connector.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import ro.atrifan.client.http.util.*;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.client.worker.HttpWorkQueue;
import ro.atrifan.client.worker.NIOHttp;
import ro.atrifan.client.worker.NIOWorker;
import ro.atrifan.model.DrawingModel;
import ro.atrifan.model.MessageModel;
import org.apache.log4j.Logger;
import ro.idp.dashboard.util.PropertiesReader;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class DashBoardService {
    private static DashBoardService instance = null;

    public static DashBoardService getInstance() {
        if(instance == null) {
            try {
                instance = new DashBoardService();
            } catch (IOException e) {
                LOG.error("Failed to instantiate UserService ", e);
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

    private static Logger LOG = Logger.getLogger(DashBoardService.class);
    private PropertiesReader propertiesReader;

    private DashBoardService() throws IOException {
        propertiesReader = PropertiesReader.load();
    }


    public HTTPResponse sendMessage(String group, MessageModel messageModel, CallBack postExecution) throws IOException {
        LOG.info(String.format("Sending message on dashBoard <{%s}>", group));

        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainDashBoardPath()).
                addPathParameter(group).addPath("/message");

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Content-Type", "application/json")
                .add("Accept", "application/json");
        HTTPPostMethod post = new HTTPPostMethod(urlBuilder.build(), headerBuilder, messageModel);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(post, timeout);
        return post.getResponse();
    }

    public HTTPResponse sendDrawing(String group, DrawingModel drawingModel, CallBack postExecution) throws IOException {
        LOG.info(String.format("Sending drawing to dashBoard <{%s}>", group));

        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainDashBoardPath()).
                addPathParameter(group).addPath("/drawing");

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Content-Type", "application/json")
                .add("Accept", "application/json");
        HTTPPostMethod post = new HTTPPostMethod(urlBuilder.build(), headerBuilder, drawingModel);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(post, timeout);
        return post.getResponse();
    }

    public HTTPResponse getDashBoard(String group) throws IOException {
        LOG.info(String.format("Sending drawing to dashBoard <{%s}>", group));

        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainDashBoardPath()).
                addPathParameter(group);

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }
}
