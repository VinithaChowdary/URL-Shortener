package com.example.URLShortener.service;

import com.example.URLShortener.repository.UrlRepository;
import com.example.URLShortener.util.Base62Encoder;

public class UrlService {

    private UrlRepository repository = new UrlRepository();

    
    public String createShortUrl(String longUrl) throws Exception {

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

        String longUrl =
            repository.findLongUrlByShortCode(shortCode);

        if (longUrl == null) {
            return null;
        }

        repository.incrementClickCount(shortCode);
        return longUrl;
    }
}
