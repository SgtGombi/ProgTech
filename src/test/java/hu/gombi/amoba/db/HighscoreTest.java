package hu.gombi.amoba.db;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class HighscoreTest {

    private final Path dbPath = Path.of("target", "test-highscores.db");

    @AfterEach
    public void cleanup() throws Exception {
        try {
            File f = dbPath.toFile();
            if (f.exists()) f.delete();
        } catch (Exception e) { /* ignore */ }
    }

    @Test
    public void addWinAndTopWorks() throws Exception {
        String url = "jdbc:sqlite:" + dbPath.toString();
        try (Highscore hs = new Highscore(url)) {
            hs.addWin("A");
            hs.addWin("B");
            hs.addWin("A");
            Map<String,Integer> top = hs.top();
            // A should have 2 wins, B 1 win; order A then B
            assertEquals(2, top.size());
            var it = top.entrySet().iterator();
            var e1 = it.next();
            assertEquals("A", e1.getKey());
            assertEquals(2, e1.getValue());
            var e2 = it.next();
            assertEquals("B", e2.getKey());
            assertEquals(1, e2.getValue());
        }
    }
}
