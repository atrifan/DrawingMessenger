package ro.atrifan.client.http.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class HTTPBaseMethod implements HTTPMethod {
    private URL url;
    private Map params;
    private Map<String, String> headers;
    private String body;
    private volatile HTTPResponse response;

    protected HTTPBaseMethod(URL url) {
        this.url = url;
        this.params = new LinkedHashMap(3);
        this.headers = new LinkedHashMap(3);
    }

    protected abstract String buildPath(String path) throws IOException;

    protected String getRequestString() throws IOException {
        String path = getURL().getPath();
        if (path == null || path.length() == 0) {
            path = "/";
        }

        return getMethodName() + " " + buildPath(path) + " HTTP/1.0\r\n";
    }

    protected String getHeaderString() {
        StringBuffer sb = new StringBuffer();
        for(String key : this.headers.keySet()) {
            sb.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }

        sb.append("\r\n");
        return sb.toString();
    }

    protected String getParameterString() throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = this.params.values().iterator(); i.hasNext(); ) {
            NameValuePair pair = (NameValuePair) i.next();
            String value = pair.getValue();
            value = URLEncoder.encode(value, "ISO-8859-1");
            sb.append(pair.getKey()).append("=").append(value);
            if (i.hasNext()) {
                sb.append("&");
            }
        }

        return sb.toString();
    }

    public Set getParams() {
        return new HashSet(this.params.values());
    }

    public Set getHeaders() {
        return new HashSet(this.headers.values());
    }

    public URL getURL() {
        return this.url;
    }

    public void addHeader(NameValuePair header) {
        if (header != null) {
            this.headers.put(header.getKey(), header.getValue());
        }
    }

    public void addParameter(NameValuePair param) {
        if (param != null) {
            this.params.put(param.getKey(), param);
        }
    }

    public void setHeaders(Map headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyString() {
        return body + "\r\n";
    }

    public NameValuePair getParameter(String key) {
        return (NameValuePair) this.params.get(key);
    }

    public HTTPResponse getResponse() {
        return response;
    }

    public void setResponse(HTTPResponse response) {
        this.response = response;
    }
}