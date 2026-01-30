package com.example.URLShortener.controller;

import com.example.URLShortener.service.UrlService;
import com.example.URLShortener.util.RedisUtil;
import com.sun.net.httpserver.HttpServer;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RedirectController {

    public static void startServer() throws IOException {

        HttpServer server =
            HttpServer.create(new InetSocketAddress(8080), 0);

        UrlService service = new UrlService();

        server.createContext("/", exchange -> {

            // 🔹 STEP 1: RATE LIMITING (per IP, per minute)
            String clientIp =
                exchange.getRemoteAddress().getAddress().getHostAddress();

            try (Jedis jedis = RedisUtil.getClient()) {

                String rateKey = "rate:" + clientIp;


                // Use Lua to atomically increment and set TTL
                String lua = "local c=redis.call('INCR', KEYS[1]); if c==1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return c;";
                long windowSeconds = 60;
                Object evalResult = jedis.eval(lua, 1, rateKey, String.valueOf(windowSeconds));
                Long requestCount = (Long) evalResult;

                long ttl = jedis.ttl(rateKey);
                System.out.println("[Redis][RateLimit] IP=" + clientIp + " count=" + requestCount + " ttl=" + ttl + "s");

                if (requestCount > 10) {
                    System.out.println("[Redis][RateLimit] BLOCK IP=" + clientIp + " count=" + requestCount);
                    exchange.sendResponseHeaders(429, -1);
                    exchange.close();
                    return;
                }

                if (requestCount > 10) {
                    // Too many requests
                    exchange.sendResponseHeaders(429, -1);
                    return;
                }
            }

            // 🔹 STEP 2: Extract short code from URL
            String path = exchange.getRequestURI().getPath();
            String shortCode = path.substring(1); // "/4" → "4"

            if (shortCode.isEmpty()) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            try {
                // 🔹 STEP 3: Resolve long URL (Redis → DB fallback)
                String longUrl =
                    service.getLongUrlAndTrackClick(shortCode);

                if (longUrl == null) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }

                // 🔹 STEP 4: HTTP 302 Redirect
                exchange.getResponseHeaders().add("Location", longUrl);
                exchange.sendResponseHeaders(302, -1);
                exchange.close();

            } catch (Exception e) {
                // 🔹 STEP 5: Internal server error
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
            }
        });

        server.start();
        System.out.println("🚀 Server started on http://localhost:8080");
    }
}
