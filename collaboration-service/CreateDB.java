import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "password";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE codesync_collab");
            System.out.println("Database codesync_collab created successfully");
        } catch (Exception e) {
            System.out.println("Could not create database. It might already exist or postgres is not running: " + e.getMessage());
        }
    }
}
