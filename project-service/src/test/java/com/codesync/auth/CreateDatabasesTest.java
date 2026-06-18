package com.codesync.auth;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDatabasesTest {
    
    @Test
    public void createDBs() {
        String url = "jdbc:postgresql://pg-18fb8c96-gargabhishek742-98ec.h.aivencloud.com:12659/defaultdb?sslmode=require";
        String user = "avnadmin";
        String password = "REDACTED";

        String[] databases = {
            "codesync_auth",
            "codesync_project",
            "codesync_file",
            "codesync_version",
            "codesync_notification",
            "codesync_execution",
            "codesync_comment"
        };

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            conn.setAutoCommit(true);

            for (String db : databases) {
                try {
                    System.out.println("Executing: CREATE DATABASE " + db);
                    stmt.executeUpdate("CREATE DATABASE " + db);
                    System.out.println("Success: " + db);
                } catch (Exception e) {
                    System.out.println("Skipped or failed " + db + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
