package org.yourcompany.yourproject.backend.dataAccessLayer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    // Updated Neon PostgreSQL URL
    private static final String URL = "jdbc:postgresql://ep-crimson-meadow-ahlv7atz-pooler.c-3.us-east-1.aws.neon.tech:5432/circuit_designer?sslmode=require&channel_binding=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_FPzto6rCfmH9";

    static {
        try {
            // Load PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ PostgreSQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("✗ ERROR: PostgreSQL JDBC Driver not found!");
            System.out.println("  Add this to your pom.xml:");
            System.out.println("  <dependency>");
            System.out.println("    <groupId>org.postgresql</groupId>");
            System.out.println("    <artifactId>postgresql</artifactId>");
            System.out.println("    <version>42.6.0</version>");
            System.out.println("  </dependency>");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        System.out.println("🔗 Attempting Neon PostgreSQL connection...");
        System.out.println("  URL: " + URL.replaceAll("npg_.+", "npg_***")); // Hide password in logs

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Neon PostgreSQL connection SUCCESSFUL");
            return conn;
        } catch (SQLException e) {
            System.out.println("✗ NEON POSTGRESQL CONNECTION FAILED!");
            System.out.println("  Error: " + e.getMessage());
            System.out.println("  SQL State: " + e.getSQLState());

            if (e.getMessage().contains("authentication failed")) {
                System.out.println("💡 SOLUTION: Check your Neon.tech credentials");
            } else if (e.getMessage().contains("Connection refused")) {
                System.out.println("💡 SOLUTION: Check your internet connection and Neon.tech status");
            }

            return null;
        }
    }

    public static boolean testConnection() {
        System.out.println("\n🧪 Testing Neon PostgreSQL Connection...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Neon PostgreSQL connection test: SUCCESS");
                return true;
            } else {
                System.out.println("✗ Neon PostgreSQL connection test: FAILED");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("✗ Neon PostgreSQL connection test: FAILED with exception");
            e.printStackTrace();
            return false;
        }
    }
}
