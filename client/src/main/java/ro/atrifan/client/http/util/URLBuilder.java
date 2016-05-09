package ro.atrifan.client.http.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class URLBuilder {

    private StringBuilder sb = new StringBuilder();
    private boolean firstQueryParam = true;
    private StringBuilder path = new StringBuilder();
    private StringBuilder query = new StringBuilder();
    public URLBuilder(String host){
        if(host.endsWith("/")) {
            host = host.substring(0, host.length() - 2);
        }
        sb.append(host);
    }

    public URLBuilder addPath(String location) {
        path.append("/").append(location.indexOf("/") == 0 ? location.replaceFirst("/", "") : location);
        return this;
    }

    public URLBuilder addPathParameter(String pathParam) {
        path.append("/").append(pathParam.indexOf("/") == 0 ? pathParam.replaceFirst("/", "") : pathParam);
        return this;
    }

    public URLBuilder addQueryParam(String queryName, String queryValue) {
        if(firstQueryParam) {
            query.append("?").append(queryName).append("=").append(queryValue);
            firstQueryParam = false;
        } else {
            query.append("&").append(queryName).append("=").append(queryValue);
        }

        return this;
    }

    public URL build() throws MalformedURLException {
        return new URL(sb.append(path).append(query).toString());
    }
}
