package com.college.timetable;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public final class Database {
    private static String url = "jdbc:mysql://localhost:3306/college_timetable?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static String username = "root";
    private static String password = "";

    private Database() {}

    public static void configure() {
        Properties p = new Properties();
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                p.load(in);
                url = p.getProperty("db.url", url);
                username = p.getProperty("db.username", username);
                password = p.getProperty("db.password", password);
            }
        } catch (Exception e) { throw new RuntimeException("Could not read db.properties: " + e.getMessage(), e); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void initialize() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users(
                  id VARCHAR(20) PRIMARY KEY, full_name VARCHAR(100) NOT NULL,
                  subject VARCHAR(80) NOT NULL, password_hash VARCHAR(64) NOT NULL,
                  role VARCHAR(10) NOT NULL DEFAULT 'TEACHER', active BOOLEAN NOT NULL DEFAULT TRUE
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS timetable_entries(
                  id INT AUTO_INCREMENT PRIMARY KEY, day_name VARCHAR(12) NOT NULL,
                  period_no INT NOT NULL, class_name VARCHAR(30) NOT NULL,
                  subject VARCHAR(80) NOT NULL, teacher_id VARCHAR(20) NOT NULL,
                  room VARCHAR(30) NOT NULL, entry_type VARCHAR(15) NOT NULL DEFAULT 'LECTURE',
                  UNIQUE KEY uq_slot_class(day_name,period_no,class_name),
                  CONSTRAINT fk_entry_teacher FOREIGN KEY(teacher_id) REFERENCES users(id)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS absences(
                  id INT AUTO_INCREMENT PRIMARY KEY, teacher_id VARCHAR(20) NOT NULL,
                  day_name VARCHAR(12) NOT NULL, reason VARCHAR(255), status VARCHAR(15) NOT NULL DEFAULT 'ABSENT',
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  UNIQUE KEY uq_absence(teacher_id,day_name)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS substitutions(
                  id INT AUTO_INCREMENT PRIMARY KEY, timetable_entry_id INT NOT NULL,
                  day_name VARCHAR(12) NOT NULL, period_no INT NOT NULL, class_name VARCHAR(30) NOT NULL,
                  subject VARCHAR(80) NOT NULL, original_teacher_id VARCHAR(20) NOT NULL,
                  substitute_teacher_id VARCHAR(20) NOT NULL, assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_sub_entry FOREIGN KEY(timetable_entry_id) REFERENCES timetable_entries(id),
                  CONSTRAINT fk_sub_teacher FOREIGN KEY(substitute_teacher_id) REFERENCES users(id),
                  UNIQUE KEY uq_sub_slot(timetable_entry_id)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS college_events(
                  id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(120) NOT NULL, day_name VARCHAR(12) NOT NULL,
                  period_no INT NOT NULL, class_name VARCHAR(30) NOT NULL, venue VARCHAR(80),
                  total_students INT NOT NULL DEFAULT 60, present_students INT NOT NULL DEFAULT 0,
                  notes VARCHAR(255)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS merged_classes(
                  id INT AUTO_INCREMENT PRIMARY KEY, class_one VARCHAR(30) NOT NULL, class_two VARCHAR(30) NOT NULL,
                  day_name VARCHAR(12) NOT NULL, period_no INT NOT NULL, subject VARCHAR(80) NOT NULL,
                  teacher_id VARCHAR(20) NOT NULL, room VARCHAR(30) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");
        }
        seedIfEmpty();
    }

    private static void seedIfEmpty() throws SQLException {
        try (Connection c = getConnection()) {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                rs.next(); if (rs.getInt(1) > 0) return;
            }
            String[][] teachers = {
                {"T1","A. Sharma","Data Structures","password123"},
                {"T2","R. Verma","DBMS","password123"},
                {"T3","S. Khan","Operating Systems","password123"},
                {"T4","P. Iyer","Computer Networks","password123"},
                {"T5","K. Rao","Java Programming","password123"},
                {"T6","N. Singh","Software Engineering","password123"}
            };
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO users(id,full_name,subject,password_hash,role) VALUES(?,?,?,?, 'TEACHER')");
                 PreparedStatement admin = c.prepareStatement("INSERT INTO users(id,full_name,subject,password_hash,role) VALUES('admin','System Administrator','Administration',?,'ADMIN')")) {
                for (String[] t : teachers) {
                    ps.setString(1,t[0]); ps.setString(2,t[1]); ps.setString(3,t[2]); ps.setString(4,Hashing.sha256(t[3])); ps.addBatch();
                }
                ps.executeBatch(); admin.setString(1,Hashing.sha256("admin123")); admin.executeUpdate();
            }
            String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
            String[] subjects = {"Data Structures","DBMS","Operating Systems","Computer Networks","Java Programming","Software Engineering"};
            String[] rooms = {"Room 101","Room 102","Lab 1","Room 204","Room 205","Lab 2"};
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO timetable_entries(day_name,period_no,class_name,subject,teacher_id,room,entry_type) VALUES(?,?,?,?,?,?,?)")) {
                for (int d=0; d<days.length; d++) for (int period=1; period<=6; period++) {
                    int idx=(period-1+d)%6;
                    ps.setString(1,days[d]); ps.setInt(2,period); ps.setString(3,"CSE-A");
                    ps.setString(4,subjects[idx]); ps.setString(5,"T"+(idx+1)); ps.setString(6,rooms[idx]);
                    ps.setString(7,period==3 || period==6 ? "LAB" : "LECTURE"); ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public static String hash(String value) { return Hashing.sha256(value); }
}
