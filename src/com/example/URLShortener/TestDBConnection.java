package com.example.URLShortener;

import com.example.URLShortener.util.DatabaseUtil;
import java.sql.Connection;

public class TestDBConnection {

    public static void main(String[] args) {
        try {
            Connection conn = DatabaseUtil.getConnection();
            System.out.println("✅ Connected successfully!");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Connection failed");
            e.printStackTrace();
        }
    }
}
