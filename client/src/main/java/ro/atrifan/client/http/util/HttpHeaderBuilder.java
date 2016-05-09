package ro.atrifan.client.http.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class HttpHeaderBuilder {

    private Map<String, String> headers = new HashMap();

    public HttpHeaderBuilder add(String headerName, String value) {
        headers.put(headerName, value);
        return this;
    }

    public Map<String, String> build() {
        return headers;
    }
}
