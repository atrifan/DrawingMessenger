package ro.atrifan.client.http.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;

public class HTTPParser {
    private static final Logger log = Logger.getLogger(HTTPParser.class);
    private static final Pattern NEWLINE = Pattern.compile("\n");
    private static final Pattern HEADER_LINE =
        Pattern.compile("HTTP/(1.[0-9]) ([0-9]{3})(?: [0-9]{3})? ([A-Za-z ]*)");

    private int code;
    private List headers;
    private StringBuffer data;
    private HTTPMethod method;
    private int contentLength;
    private boolean headerComplete;
    private boolean statusComplete;

    public HTTPParser(HTTPMethod method) {
        if (method == null) {
            throw new NullPointerException();
        }

        this.code = 0;
        this.method = method;
        this.contentLength = -1;
        this.headerComplete = false;
        this.statusComplete = false;
        this.headers = new LinkedList();
        this.data = new StringBuffer();
    }

    public void feed(String data) {
        if (data == null) {
            this.contentLength = this.data.length();
            return;
        }

        this.data.append(data);

        if (!this.statusComplete) {
            int index = this.data.indexOf("\n");
            if (index > 0) {
                String header = this.data.substring(0, index).trim();
                Matcher m = HEADER_LINE.matcher(header);
                if (m.matches()) {
                    this.statusComplete = true;
                    this.code = Integer.parseInt(m.group(2));
                    this.data = new StringBuffer(this.data.substring(index + 1));
                }
            }
        }

        if (this.statusComplete && !this.headerComplete) {
            int indexBreak = this.data.indexOf("\r\n\r\n");
            int breakLength = 4;
            if (indexBreak == -1) {
                indexBreak = this.data.indexOf("\r\n");
                breakLength = 2;
            }

            if (indexBreak > 0) {
                String header = this.data.substring(0, indexBreak);
                String[] lines = NEWLINE.split(header);

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.length() == 0) {
                        continue;
                    }

                    int indexHeaderSep = line.indexOf(":");
                    if (indexHeaderSep == -1) {
                        log.warn("no colon in header line: " + line);
                        continue;
                    }

                    String key = line.substring(0, indexHeaderSep).trim();
                    String value = line.substring(indexHeaderSep + 1).trim();
                    if (key.toLowerCase().equals("content-length")) {
                        this.contentLength = Integer.parseInt(value);
                    }

                    this.headers.add(new NameValuePair(key, value));
                }

                this.headerComplete = true;
                this.data = new StringBuffer(this.data.substring(indexBreak + breakLength));
            } else if (indexBreak == 0) {
                this.headerComplete = true;
            }
        }
    }

    public boolean isHeaderComplete() {
        return this.headerComplete;
    }

    public boolean isStatusComplete() {
        return this.statusComplete;
    }

    public boolean isBodyComplete() {
        return this.data.length() == this.contentLength;
    }

    public boolean isComplete() {
        return isStatusComplete() & isHeaderComplete();
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public HTTPResponse getResponse() {
        if (!isComplete()) {
            return null;
        }

        HTTPResponse res = new HTTPResponse(this.method);
        res.setCode(this.code);

        NameValuePair[] headers = new NameValuePair[this.headers.size()];
        this.headers.toArray(headers);
        res.setHeaders(headers);

        int dataLen = this.data.length();
        if (contentLength != dataLen) {
            log.debug("content-length header is invalid. Says length is" +
                " [" + contentLength + "] but data is [" + dataLen + "]");
            log.debug(data);
        }

        res.setContentLength(dataLen);
        res.setBody(this.data.toString());

        return res;
    }
}
