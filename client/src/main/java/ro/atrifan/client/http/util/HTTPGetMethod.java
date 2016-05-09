package ro.atrifan.client.http.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.log4j.Logger;

public class HTTPGetMethod extends HTTPBaseMethod {
    private static final Logger log = Logger.getLogger(HTTPGetMethod.class);
    public HTTPGetMethod(URL url, HttpHeaderBuilder headers) {
        super(url);
        setHeaders(headers.build());
    }

    public String toHTTP() throws IOException {
        String req = getRequestString();
        String headers = getHeaderString();

        StringBuffer sb = new StringBuffer();
        sb.append(req);
        sb.append(headers);
        log.debug(sb.toString());
        return sb.toString();
    }

    protected String buildPath(String path) throws UnsupportedEncodingException {
        String params = getParameterString();
        if (params.length() > 0) {
            path = path + "?" + params;
        }

        return path;
    }

    public String getMethodName() {
        return "GET";
    }
}