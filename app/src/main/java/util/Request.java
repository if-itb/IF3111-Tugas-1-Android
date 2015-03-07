package util;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by calvin-pc on 3/7/2015.
 */
public class Request implements Callable<Response> {
    private URL url;

    public Request(URL url) {
        this.url = url;
    }

    @Override
    public Response call() throws Exception {
        return new Response(url.openStream());
    }
}