package hu.gombi.amoba.io;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.Board;

// --- Ez az osztaly teszteli a TextBoardIO osztalyt extra esetekkel
public class TextBoardIOExtraTest {

    Path tmp = Path.of("target", "test-board.txt");

    // --- Minden teszt utan takaritja a fajlt
    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(tmp);
    }

    // --- Ez a teszt ellenorzi a save es load metodusokat
    @Test
    void saveAndLoad_preservesPlayerNameAndBoard() throws Exception {
        // --- Board varakozik sorok >= oszlopok
        Board b = new Board(5, 4);
        String name = "JátékosÉkezetes";
        TextBoardIO.save(b, tmp, name);
        assertTrue(Files.exists(tmp));
        var loaded = TextBoardIO.load(tmp);
        assertNotNull(loaded);
        assertEquals(name, loaded.playerName());
        assertEquals(5, loaded.board().getRows());
        assertEquals(4, loaded.board().getCols());
    }
}
