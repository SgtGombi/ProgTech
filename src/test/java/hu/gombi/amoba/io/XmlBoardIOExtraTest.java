package hu.gombi.amoba.io;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.Board;

public class XmlBoardIOExtraTest {

    Path tmp = Path.of("target", "test-xmlboardio.xml");

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(tmp);
    }

    @Test
    void saveAndLoad_roundtrip_preservesDimensionsAndPlayerNameEscaped() throws Exception {
        Board b = new Board(4, 4);
        String player = "Ákos & <Tester> \"'";
        XmlBoardIO.save(b, tmp, player);

        assertTrue(Files.exists(tmp), "XML file should have been created");

        // file should contain escaped forms like &amp; and &lt;
        String content = Files.readString(tmp);
        assertTrue(content.contains("&amp;") || content.contains("&lt;") || content.contains("&quot;"), "Saved XML should escape special chars");

        var saved = XmlBoardIO.load(tmp);
        assertNotNull(saved, "Loaded SavedGame should not be null");
        assertEquals(4, saved.board().getRows());
        assertEquals(4, saved.board().getCols());
        // load returns the raw tag content which was escaped on save
        assertTrue(saved.playerName().contains("&amp;") || saved.playerName().contains("&lt;") || saved.playerName().contains("&quot;"));
    }
}
