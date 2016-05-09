package ro.idp.dashboard.connector.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.atrifan.client.http.util.*;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.client.worker.NIOHttp;
import org.apache.log4j.Logger;
import ro.atrifan.client.worker.HttpWorkQueue;
import ro.idp.dashboard.util.PropertiesReader;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class GroupService{

    private static GroupService instance = null;

    public static GroupService getInstance() {
        if(instance == null) {
            try {
                instance = new GroupService();
            } catch (IOException e) {
                LOG.error("Failed to instantiate UserService ", e);
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

    private static Logger LOG = Logger.getLogger(GroupService.class);
    private PropertiesReader propertiesReader;
    private GroupService() throws IOException {
        propertiesReader = PropertiesReader.load();
    }


    public HTTPResponse createGroup(String group, CallBack postExecution) throws IOException {
        LOG.info(String.format("Creating group <{%s}>", group));

        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainGroupPath()).addPathParameter(group);

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Content-Type", "application/json")
                .add("Accept", "application/json");
        HTTPPostMethod post = new HTTPPostMethod(urlBuilder.build(), headerBuilder, null);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(post, timeout);
        return post.getResponse();
    }

    public HTTPResponse addUserToGroup(String groupName, String userName, CallBack callBack) throws IOException {
        LOG.info(String.format("Adding user <{%s}> to group <{%s}>", userName, groupName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainGroupPath()).addPathParameter(groupName).addPathParameter(userName);

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPPutMethod put = new HTTPPutMethod(urlBuilder.build(), headerBuilder, null);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(put, timeout);
        return put.getResponse();
    }

    public HTTPResponse getUsersNotInGroup(String groupName, CallBack callBack) throws IOException {
        LOG.info(String.format("Getting users of group <{%s}>", groupName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainGroupPath()).addPathParameter(groupName).addPath("/free");

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 600 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }

    public HTTPResponse getUsersOfGroup(String groupName, CallBack callBack) throws IOException {
        LOG.info(String.format("Getting users of group <{%s}>", groupName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainGroupPath()).addPathParameter(groupName);

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }

    public HTTPResponse getGroups(CallBack callBack) throws IOException {
        LOG.info(String.format("Getting groups"));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBrainGroupPath());

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);

        return get.getResponse();
    }

}
