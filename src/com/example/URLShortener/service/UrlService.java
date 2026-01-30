package com.example.URLShortener.service;

import java.net.URI;

import com.example.URLShortener.repository.UrlRepository;
import com.example.URLShortener.util.Base62Encoder;
import com.example.URLShortener.util.RedisUtil;

import redis.clients.jedis.Jedis;

public class UrlService {

    private UrlRepository repository = new UrlRepository();

    
    public String createShortUrl(String longUrl) throws Exception {

        ensureValidUrl(longUrl);

        // 1️⃣ Save URL → get auto-generated ID
        long id = repository.saveAndReturnId(longUrl);

        // 2️⃣ Encode ID → Base62 short code
        String shortCode = Base62Encoder.encode(id);

        // 3️⃣ Store short code in DB
        repository.updateShortCode(id, shortCode);

        // 4️⃣ Return full short URL
        return "http://localhost:8080/" + shortCode;

        
    }


    public String getLongUrlAndTrackClick(String shortCode) throws Exception {

        // 1️⃣ Check Redis cache
        try (Jedis jedis = RedisUtil.getClient()) {

            String cachedUrl = jedis.get(shortCode);

            if (cachedUrl != null) {
                // Cache HIT
                System.out.println("[Redis][CacheHit] shortCode=" + shortCode + " -> " + cachedUrl);
                repository.incrementClickCount(shortCode);
                return cachedUrl;
            }

            System.out.println("[Redis][CacheMiss] shortCode=" + shortCode);
        }

        // 2️⃣ Cache MISS → DB lookup
        String longUrl =
            repository.findLongUrlByShortCode(shortCode);

        if (longUrl == null) {
            return null;
        }

        // 3️⃣ Populate cache
        try (Jedis jedis = RedisUtil.getClient()) {
            jedis.set(shortCode, longUrl);
            System.out.println("[Redis][CacheWrite] shortCode=" + shortCode + " -> " + longUrl);
        }

        // 4️⃣ Track click
        repository.incrementClickCount(shortCode);
        return longUrl;
    }

    private void ensureValidUrl(String url) {
        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();
            String host = uri.getHost();

            boolean schemeOk = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            boolean hostOk = host != null && host.contains(".");

            if (!schemeOk || !hostOk) {
                throw new IllegalArgumentException("Invalid URL: must be http/https with a valid host");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
    }


}
