package ro.idp.dashboard.connector.http;

import ro.atrifan.client.worker.NIOHttp;
import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.*;
import ro.atrifan.client.worker.CallBack;
import ro.atrifan.client.worker.HttpWorkQueue;
import ro.atrifan.client.worker.NIOWorker;
import ro.atrifan.model.LoginModel;
import ro.idp.dashboard.util.PropertiesReader;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class UserService {

    private static UserService instance = null;

    public static UserService getInstance() {
        if(instance == null) {
            try {
                instance = new UserService();
            } catch (IOException e) {
                LOG.error("Failed to instantiate UserService ", e);
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

    private static Logger LOG = Logger.getLogger(UserService.class);
    private PropertiesReader propertiesReader;
    private UserService() throws IOException {
        propertiesReader = PropertiesReader.load();
    }

    public HTTPResponse login(LoginModel loginModel, CallBack callBack) throws IOException {
        LOG.info(String.format("Logging in <{%s}>", loginModel.getUserName()));

        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBraingUserPath());

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Content-Type", "application/json")
                .add("Accept", "application/json");
        HTTPPostMethod post = new HTTPPostMethod(urlBuilder.build(), headerBuilder, loginModel);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(post, timeout);
        return post.getResponse();
    }

    public HTTPResponse logout(String userName, CallBack callBack) throws IOException {
        LOG.info(String.format("Logging out <{%s}>", userName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBraingUserPath()).addPathParameter(userName).addPath("/logout");

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }

    public HTTPResponse getUsers(CallBack callBack) throws IOException {
        LOG.info("Getting all users");
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBraingUserPath());

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }

    public HTTPResponse getUser(String userName) throws IOException {
        LOG.info(String.format("Getting user <%s>", userName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBraingUserPath()).addPathParameter(userName);

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }

    public HTTPResponse getSubscribedGroupsOfUser(String userName, CallBack callBack) throws IOException {
        LOG.info(String.format("Getting subscribed groups of user <{%s}>", userName));
        URLBuilder urlBuilder = new URLBuilder(propertiesReader.getBrainHost());
        urlBuilder.addPath(propertiesReader.getBraingUserPath()).addPathParameter(userName)
                .addPath("/groups");

        HttpHeaderBuilder headerBuilder = new HttpHeaderBuilder();
        headerBuilder.add("Accept", "application/json");
        HTTPGetMethod get = new HTTPGetMethod(urlBuilder.build(), headerBuilder);

        long timeout = 60 * 1000; // ten seconds

        NIOHttp.getInstance().execute(get, timeout);
        return get.getResponse();
    }
}
