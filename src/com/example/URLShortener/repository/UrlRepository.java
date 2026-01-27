package com.example.URLShortener.repository;

import com.example.URLShortener.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UrlRepository {

    public void save(String shortCode, String longUrl) throws Exception {

        String sql =
            "INSERT INTO url_mapping (short_code, long_url) VALUES (?, ?)";

        try (Connection conn = com.example.URLShortener.util.DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, shortCode);
            ps.setString(2, longUrl);
            ps.executeUpdate();
        }
    }


    public long saveAndReturnId(String longUrl) throws Exception {

        String sql =
            "INSERT INTO url_mapping (long_url) VALUES (?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, longUrl);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

            throw new RuntimeException("Failed to generate ID");
        }
    }

    public void updateShortCode(long id, String shortCode) throws Exception {

        String sql =
            "UPDATE url_mapping SET short_code = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, shortCode);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public String findLongUrlByShortCode(String shortCode) throws Exception {

        String sql =
            "SELECT long_url FROM url_mapping WHERE short_code = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, shortCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("long_url");
            }
            return null;
        }
    }

    public void incrementClickCount(String shortCode) throws Exception {

        String sql =
            "UPDATE url_mapping SET click_count = click_count + 1 WHERE short_code = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, shortCode);
            ps.executeUpdate();
        }
    }
}
