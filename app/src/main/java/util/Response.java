package util;

import java.io.InputStream;

/**
 * Created by calvin-pc on 3/7/2015.
 */
public class Response {
    private InputStream body;

    public Response(InputStream body) {
        this.body = body;
    }

    public InputStream getBody() {
        return body;
    }
}