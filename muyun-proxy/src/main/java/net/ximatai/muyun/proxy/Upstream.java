package net.ximatai.muyun.proxy;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Upstream {
    private static final Logger LOGGER = LoggerFactory.getLogger(Upstream.class);

    private final String url;
    private final int weight;
    private String path;

    private String host;
    private int port;

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public HttpClient getClient() {
        return client;
    }

    public int getWeight() {
        return weight;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    HttpClient client;

    public Upstream(JsonObject json, Vertx vertx) {
        this(json.getString("url"), json.getInteger("weight", 1), vertx);
    }

    public Upstream(String url, int weight, Vertx vertx) {

        this.url = url;
        this.weight = weight;

        try {
            URL realURL = new URI(this.url).toURL();
            host = realURL.getHost();
            port = realURL.getPort();
            this.path = realURL.getPath();

            HttpClientOptions clientOptions = new HttpClientOptions();
            clientOptions.setDefaultHost(host);
            clientOptions.setDefaultPort(port);
            clientOptions.setKeepAlive(true);
            clientOptions.setTryUsePerMessageWebSocketCompression(true);

            if (realURL.getProtocol().equals("https")) {
                clientOptions.setSsl(true);
                clientOptions.setTrustAll(true);
                clientOptions.setDefaultPort(443);
            }

            this.client = vertx.createHttpClient(clientOptions);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL: {}", this.url, e);
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URL: {}", this.url, e);
        }

    }
}
