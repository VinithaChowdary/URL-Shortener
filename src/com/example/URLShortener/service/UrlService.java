package com.example.URLShortener.service;

import java.net.URI;

import com.example.URLShortener.repository.UrlRepository;
import com.example.URLShortener.util.Base62Encoder;
import com.example.URLShortener.util.RedisUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
                incrementClickCounter(shortCode);
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

        // 4️⃣ Track click in Redis counter
        incrementClickCounter(shortCode);
        return longUrl;
    }

    private static final String CLICK_PREFIX = "click:";

    private void incrementClickCounter(String shortCode) {
        try (Jedis jedis = RedisUtil.getClient()) {
            String key = CLICK_PREFIX + shortCode;
            jedis.incr(key);
            jedis.expire(key, 3600); // keep hot counters around for an hour
        }
    }

    public void flushClickCountsToDb() {
        Map<String, Integer> deltas = new HashMap<>();

        try (Jedis jedis = RedisUtil.getClient()) {
            Set<String> keys = jedis.keys(CLICK_PREFIX + "*");

            for (String key : keys) {
                String countStr = jedis.get(key);
                if (countStr == null) {
                    continue;
                }

                int delta = Integer.parseInt(countStr);
                String shortCode = key.substring(CLICK_PREFIX.length());

                deltas.merge(shortCode, delta, Integer::sum);

                jedis.del(key);
            }

            if (!deltas.isEmpty()) {
                repository.batchIncrementClickCounts(deltas);
                System.out.println("[Sync][Clicks] Flushed " + deltas.size() + " short codes to DB");
            }

        } catch (Exception e) {
            System.err.println("[Sync][Clicks] Failed to flush click counters: " + e.getMessage());
        }
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
