package hu.gombi.amoba.io;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.Board;

// --- Ez az osztaly teszteli az XmlBoardIO osztalyt extra esetekkel
public class XmlBoardIOExtraTest {

    Path tmp = Path.of("target", "test-xmlboardio.xml");

    // --- Minden teszt utan takaritja a fajlt
    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(tmp);
    }

    // --- Ez a teszt ellenorzi a save es load fordulot es a jatekos nev escape-eleset
    @Test
    void saveAndLoad_roundtrip_preservesDimensionsAndPlayerNameEscaped() throws Exception {
        Board b = new Board(4, 4);
        String player = "Ákos & <Tester> \"'";
        XmlBoardIO.save(b, tmp, player);

        assertTrue(Files.exists(tmp), "XML fajl letrehozva");

        // --- Fajl tartalmaz escape-elt formakat mint &amp; es &lt;
        String content = Files.readString(tmp);
        assertTrue(content.contains("&amp;") || content.contains("&lt;") || content.contains("&quot;"), "Mentett XML escape-eli a specialis karaktereket");

        var saved = XmlBoardIO.load(tmp);
        assertNotNull(saved, "Betoltott SavedGame nem null");
        assertEquals(4, saved.board().getRows());
        assertEquals(4, saved.board().getCols());
        // --- Load visszaadja a nyers tag tartalmat ami escape-elve volt menteskor
        assertTrue(saved.playerName().contains("&amp;") || saved.playerName().contains("&lt;") || saved.playerName().contains("&quot;"));
    }
}
