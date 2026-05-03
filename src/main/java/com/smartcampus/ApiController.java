package com.smartcampus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DatabaseManager dbManager = new DatabaseManager();

    @GetMapping("/bookings")
    public List<Map<String, Object>> getBookings() {
        List<Map<String, Object>> bookings = new ArrayList<>();
        String sql = "SELECT b.Booking_ID, b.User_ID, b.Booking_Date, b.Start_Time, b.End_Time, b.Status, u.Name as User_Name, r.Resource_Name " +
                     "FROM Booking b " +
                     "JOIN User u ON b.User_ID = u.User_ID " +
                     "JOIN Resource r ON b.Resource_ID = r.Resource_ID";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("Booking_ID", rs.getObject("Booking_ID"));
                map.put("User_ID", rs.getObject("User_ID"));
                map.put("Booking_Date", rs.getObject("Booking_Date"));
                map.put("Start_Time", rs.getObject("Start_Time"));
                map.put("End_Time", rs.getObject("End_Time"));
                map.put("Status", rs.getObject("Status"));
                map.put("User_Name", rs.getObject("User_Name"));
                map.put("Resource_Name", rs.getObject("Resource_Name"));
                bookings.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    @GetMapping("/resources/available")
    public List<Map<String, Object>> getAvailableResources(
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Map<String, Object>> resources = new ArrayList<>();
        String sql = "SELECT Resource_ID, Resource_Name FROM Resource WHERE Status = 'Available' AND Resource_ID NOT IN " +
                     "(SELECT Resource_ID FROM Booking WHERE Booking_Date = ? AND Status != 'Rejected' AND " +
                     "(Start_Time < ? AND End_Time > ?))";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setTime(2, java.sql.Time.valueOf(endTime));
            pstmt.setTime(3, java.sql.Time.valueOf(startTime));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("Resource_ID", rs.getObject("Resource_ID"));
                map.put("Resource_Name", rs.getObject("Resource_Name"));
                resources.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resources;
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, String> payload) {
        int randomId = 100 + new java.util.Random().nextInt(900);
        String sql = "INSERT INTO Booking (Booking_ID, Booking_Date, Start_Time, End_Time, Purpose, Status, User_ID, Resource_ID) VALUES (?, ?, ?, ?, ?, 'Pending', ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, randomId);
            pstmt.setDate(2, java.sql.Date.valueOf(payload.get("date")));
            pstmt.setTime(3, java.sql.Time.valueOf(payload.get("startTime")));
            pstmt.setTime(4, java.sql.Time.valueOf(payload.get("endTime")));
            pstmt.setString(5, payload.get("purpose"));
            pstmt.setInt(6, Integer.parseInt(payload.get("userId")));
            pstmt.setInt(7, Integer.parseInt(payload.get("resourceId")));
            pstmt.executeUpdate();
            return ResponseEntity.ok(Map.of("message", "Booking created successfully! Your Booking ID is: " + randomId, "bookingId", randomId));
        } catch (Exception ex) {
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.contains("foreign key constraint fails") && errorMsg.contains("`User_ID`")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Error: User ID not found."));
            }
            return ResponseEntity.badRequest().body(Map.of("error", errorMsg));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String sql = "INSERT INTO User (User_ID, Name, Email, Role, Department_ID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(payload.get("id")));
            pstmt.setString(2, payload.get("name"));
            pstmt.setString(3, payload.get("email"));
            pstmt.setString(4, payload.get("role"));
            pstmt.setInt(5, Integer.parseInt(payload.get("deptId")));
            pstmt.executeUpdate();
            return ResponseEntity.ok(Map.of("message", "User added successfully!"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/resources")
    public ResponseEntity<?> createResource(@RequestBody Map<String, String> payload) {
        String sql = "INSERT INTO Resource (Resource_ID, Resource_Name, Resource_Type, Location, Capacity, Department_ID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(payload.get("id")));
            pstmt.setString(2, payload.get("name"));
            pstmt.setString(3, payload.get("type"));
            pstmt.setString(4, payload.get("location"));
            pstmt.setInt(5, Integer.parseInt(payload.get("capacity")));
            pstmt.setInt(6, Integer.parseInt(payload.get("deptId")));
            pstmt.executeUpdate();
            return ResponseEntity.ok(Map.of("message", "Resource added successfully!"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, String> payload) {
        String sql = "INSERT INTO Department (Department_ID, Department_Name, Block) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(payload.get("id")));
            pstmt.setString(2, payload.get("name"));
            pstmt.setString(3, payload.get("block"));
            pstmt.executeUpdate();
            return ResponseEntity.ok(Map.of("message", "Department added successfully!"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable int id, @RequestBody Map<String, String> payload) {
        String sql = "UPDATE Booking SET Status = ? WHERE Booking_ID = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, payload.get("status"));
            pstmt.setInt(2, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                return ResponseEntity.ok(Map.of("message", "Booking status updated successfully!"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking ID not found."));
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/usage-logs")
    public ResponseEntity<?> createUsageLog(@RequestBody Map<String, String> payload) {
        String sql = "INSERT INTO Usage_Log (Log_ID, Booking_ID, Actual_Start_Time, Actual_End_Time, Usage_Duration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(payload.get("logId")));
            pstmt.setInt(2, Integer.parseInt(payload.get("bookingId")));
            pstmt.setTime(3, java.sql.Time.valueOf(payload.get("startTime")));
            pstmt.setTime(4, java.sql.Time.valueOf(payload.get("endTime")));
            pstmt.setInt(5, Integer.parseInt(payload.get("duration")));
            pstmt.executeUpdate();
            return ResponseEntity.ok(Map.of("message", "Usage Log added successfully!"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
