package hu.gombi.amoba.db;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

// --- Ez az osztaly teszteli a Highscore osztalyt adatbazissal
public class HighscoreTest {
    private final Path dbPath = Path.of("target", "test-highscores.db");

    // --- Minden teszt utan takaritja a fajlt
    @AfterEach
    public void cleanup() throws Exception {
        try {
            File f = dbPath.toFile();
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    // --- Ez a teszt metodus ellenorzi az addWin es top metodusokat
    @Test
    public void addWinAndTopWorks() throws Exception {
        String url = "jdbc:sqlite:" + dbPath.toString();
        try (Highscore hs = new Highscore(url)) {
            hs.addWin("A");
            hs.addWin("B");
            hs.addWin("A");
            Map<String, Integer> top = hs.top();
            // --- A-nak 2 gyozelme, B-nek 1; sorrend A majd B
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
