package nl.is.kc.nio.client.model;

/**
 * Created by ruben on 2-4-15.
 */
public class NameServerResponse {
    private String host;
    private int port;

    public NameServerResponse(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
