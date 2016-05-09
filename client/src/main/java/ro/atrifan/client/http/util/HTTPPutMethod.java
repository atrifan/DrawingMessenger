package ro.atrifan.client.http.util;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class HTTPPutMethod extends HTTPBaseMethod {
    ObjectMapper objectMapper = new ObjectMapper();
    public HTTPPutMethod(URL url, HttpHeaderBuilder headers, Object body) throws JsonProcessingException {
        super(url);
        String jsonBody = objectMapper.writeValueAsString(body);
        headers.add("Content-Length", jsonBody.length() + "");
        setHeaders(headers.build());
    }

    public String toHTTP() throws IOException {
        String req = getRequestString();
        String body = getBodyString();
        String headers = getHeaderString();

        StringBuffer sb = new StringBuffer();
        sb.append(req);
        sb.append(headers);
        sb.append(body);
        return sb.toString();
    }

    protected String buildPath(String path) {
        return path;
    }

    public String getMethodName() {
        return "PUT";
    }
}
