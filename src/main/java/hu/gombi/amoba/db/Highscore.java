package hu.gombi.amoba.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class Highscore implements AutoCloseable {
    private final Connection conn;

    // --- konstruktor: adatbázis kapcsolat létrehozása, tábla létrehozása ha nem létezik ---
    public Highscore(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS scores(name TEXT PRIMARY KEY, wins INTEGER)");
        }
    }
    // --- győzelem hozzáadása ---
    public void addWin(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO scores(name,wins) VALUES(?,1) ON CONFLICT(name) DO UPDATE SET wins=wins+1")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
    // --- winek lekérdezése csökkenő sorrendben ---
    public Map<String,Integer> top() throws SQLException {
        Map<String,Integer> res = new LinkedHashMap<>();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT name,wins FROM scores ORDER BY wins DESC")) {
            while (rs.next()) res.put(rs.getString(1), rs.getInt(2));
        }
        return res;
    }

    @Override
    public void close() throws Exception { conn.close(); }
}