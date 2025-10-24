package hu.gombi.amoba.io;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

// --- Ez az osztaly teszteli a TextBoardIO osztalyt
public class TextBoardIOTest {

    private final Path testFile = Path.of("target", "test-board.txt");

    // --- Minden teszt utan takaritja a fajlt
    @AfterEach
    public void cleanup() throws Exception {
        try { Files.deleteIfExists(testFile); } catch (Exception e) { /* ignore */ }
    }

    // --- Ez a teszt ellenorzi a save es load metodusok koruli fordulot
    @Test
    public void saveAndLoadRoundtripIncludesPlayerName() throws Exception {
        Board b = new Board(5,4);
        b.makeMove(new Move(new Position(0,0), Cell.X));
        b.makeMove(new Move(new Position(1,2), Cell.O));
        String player = "TesztJatekos";
        TextBoardIO.save(b, testFile, player);

        TextBoardIO.SavedGame sg = TextBoardIO.load(testFile);
        assertNotNull(sg, "Loaded saved game should not be null");
        assertEquals(player, sg.playerName());
        Board loaded = sg.board();
        assertEquals(Cell.X, loaded.cellAt(0,0));
        assertEquals(Cell.O, loaded.cellAt(1,2));
        // --- Biztosítja, hogy a tobbi cella ures
        assertEquals(Cell.EMPTY, loaded.cellAt(0,1));
    }

    // --- Ez a teszt ellenorzi a hianyzo fajl betolteset
    @Test
    public void loadMissingFileReturnsNull() throws Exception {
        Files.deleteIfExists(testFile);
        TextBoardIO.SavedGame sg = TextBoardIO.load(testFile);
        assertNull(sg);
    }
}
