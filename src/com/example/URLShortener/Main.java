package com.example.URLShortener;

import com.example.URLShortener.service.UrlService;
import com.example.URLShortener.controller.RedirectController;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {

        UrlService service = new UrlService();

        // Background sync job: flush Redis click counters to PostgreSQL every 30s
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                service.flushClickCountsToDb();
            } catch (Exception e) {
                System.err.println("[Sync][Scheduler] Error: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            service.flushClickCountsToDb();
        }));

        // 🔹 Create new short URLs (adds rows to DB)
        String s1 = service.createShortUrl("https://google.com");
        String s2 = service.createShortUrl("https://openai.com");

        System.out.println("Created: " + s1);
        System.out.println("Created: " + s2);

        RedirectController.startServer();
    }
}

