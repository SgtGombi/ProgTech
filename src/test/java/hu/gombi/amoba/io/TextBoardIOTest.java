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

public class TextBoardIOTest {

    private final Path testFile = Path.of("target", "test-board.txt");

    @AfterEach
    public void cleanup() throws Exception {
        try { Files.deleteIfExists(testFile); } catch (Exception e) { /* ignore */ }
    }

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
        // ensure other cells are empty
        assertEquals(Cell.EMPTY, loaded.cellAt(0,1));
    }

    @Test
    public void loadMissingFileReturnsNull() throws Exception {
        Files.deleteIfExists(testFile);
        TextBoardIO.SavedGame sg = TextBoardIO.load(testFile);
        assertNull(sg);
    }
}
