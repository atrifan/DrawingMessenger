package ro.atrifan.client.http.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class HTTPDeleteMethod extends HTTPBaseMethod {
    private static final Logger log = Logger.getLogger(HTTPGetMethod.class);
    public HTTPDeleteMethod(URL url, HttpHeaderBuilder headers) {
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
        return "DELETE";
    }
}
