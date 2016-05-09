package ro.atrifan.client.http.util;

import java.io.IOException;
import java.net.URL;

public interface HTTPMethod {
    URL getURL();

    String toHTTP() throws IOException;

    String getMethodName();

    void addHeader(NameValuePair header);

    void addParameter(NameValuePair param);

    NameValuePair getParameter(String key);

    HTTPResponse getResponse();

    void setResponse(HTTPResponse response);
}