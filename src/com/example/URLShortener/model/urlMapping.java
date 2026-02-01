package com.example.URLShortener.model;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class urlMapping {
    private long id;
    private String shortCode;
    private String longUrl;
    private LocalDateTime createdAt;
    private int clickCount;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getClickCount() { return clickCount; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
}



