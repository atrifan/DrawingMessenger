package ro.atrifan.client.http.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class HTTPPostMethod extends HTTPBaseMethod {
    ObjectMapper objectMapper = new ObjectMapper();
    Logger LOG = Logger.getLogger(HTTPPostMethod.class);
    public HTTPPostMethod(URL url, HttpHeaderBuilder headers, Object body) throws JsonProcessingException {
        super(url);
        String jsonBody;
        if(body == null) {
            jsonBody = "{}";
        } else {
            jsonBody = objectMapper.writeValueAsString(body);
        }

        setBody(jsonBody);
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

        LOG.info(String.format("Making POST request to <{%s}> -- <{%s}> headers <{%s}> body <%s>", getURL().getHost(), req, headers, body));
        return sb.toString();
    }

    protected String buildPath(String path) {
        return path;
    }

    public String getMethodName() {
        return "POST";
    }
}