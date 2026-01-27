package com.example.URLShortener.controller;

import com.example.URLShortener.service.UrlService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RedirectController {

    public static void startServer() throws IOException {

        HttpServer server =
            HttpServer.create(new InetSocketAddress(8080), 0);

        UrlService service = new UrlService();

        server.createContext("/", exchange -> {

            // Extract path: "/4" → "4"
            String path = exchange.getRequestURI().getPath();
            String shortCode = path.substring(1);

            if (shortCode.isEmpty()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            try {
                String longUrl =
                    service.getLongUrlAndTrackClick(shortCode);

                if (longUrl == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                // HTTP 302 Redirect
                exchange.getResponseHeaders().add("Location", longUrl);
                exchange.sendResponseHeaders(302, -1);

            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.start();
        System.out.println("🚀 Server started on http://localhost:8080");
    }
}
