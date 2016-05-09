package ro.atrifan.client.http.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class HTTPResponse {
    private int code;
    private String body;
    private Set headers;
    private long runningTime;
    private int contentLength;
    private HTTPMethod method;

    public HTTPResponse(HTTPMethod method) {
        this.method = method;
        this.runningTime = -1L;
    }

    public int getCode() {
        return code;
    }

    void setCode(int code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    void setBody(String body) {
        this.body = body;
    }

    public NameValuePair[] getHeaders() {
        NameValuePair[] headers = new NameValuePair[this.headers.size()];
        this.headers.toArray(headers);
        return headers;
    }

    void setHeaders(NameValuePair[] headers) {
        if (headers == null) {
            this.headers = new LinkedHashSet(Arrays.asList(headers));
        }
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public long getRunningTime() {
        return this.runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
    }

    public int getContentLength() {
        return contentLength;
    }

    void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
}