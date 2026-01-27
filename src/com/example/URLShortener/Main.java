package com.example.URLShortener;

import com.example.URLShortener.service.UrlService;
import com.example.URLShortener.controller.RedirectController;

public class Main {

    public static void main(String[] args) throws Exception {

        UrlService service = new UrlService();

        // 🔹 Create new short URLs (adds rows to DB)
        String s1 = service.createShortUrl("https://google.com");
        String s2 = service.createShortUrl("https://openai.com");

        System.out.println("Created: " + s1);
        System.out.println("Created: " + s2);

        RedirectController.startServer();
    }
}
