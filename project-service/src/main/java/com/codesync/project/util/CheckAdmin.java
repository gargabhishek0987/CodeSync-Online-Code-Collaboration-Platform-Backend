package com.codesync.auth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckAdmin {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://pg-18fb8c96-gargabhishek742-98ec.h.aivencloud.com:12659/codesync_auth?sslmode=require";
        String user = "avnadmin";
        String password = "REDACTED";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT username, email, role FROM users WHERE username = 'admin'");
            if (rs.next()) {
                System.out.println("USER FOUND: " + rs.getString("username"));
                System.out.println("ROLE IN DB: " + rs.getString("role"));
                
                if (!"ADMIN".equals(rs.getString("role"))) {
                    System.out.println("Fixing role to ADMIN...");
                    stmt.executeUpdate("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'");
                    System.out.println("Role fixed!");
                }
            } else {
                System.out.println("USER 'admin' NOT FOUND IN DATABASE!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
