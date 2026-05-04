package com.smartcampus;

import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Department LIMIT 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.println(meta.getColumnName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
