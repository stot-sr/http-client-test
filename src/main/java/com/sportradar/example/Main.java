package com.sportradar.example;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Main {
    private static final Logger LOGGER = Logger.getLogger("main");
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        LOGGER.info("This case will fail");
        sendRequests(20, 15);

        LOGGER.info("This case will succeed");
        sendRequests(30, 30);

        executor.shutdown();
    }

    private static void sendRequests(int maxConnTotal, int maxConnPerRoute) {
        int maxTimeout = Math.toIntExact(TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));

        RequestConfig.Builder requestBuilder = RequestConfig.custom()
                .setConnectTimeout(maxTimeout)
                .setConnectionRequestTimeout(maxTimeout)
                .setSocketTimeout(maxTimeout);
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(requestBuilder.build())
                .setMaxConnTotal(maxConnTotal)
                .setMaxConnPerRoute(maxConnPerRoute)
                .build();

        IntStream.range(0, 20)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        httpClient.execute(new HttpGet("https://www.google.com/"));
                        LOGGER.info("Success: " + i);
                    } catch (IOException e) {
                        LOGGER.severe("Failed: " + i);
                    }
                }, executor))
                .forEach(CompletableFuture::join);
    }
}
