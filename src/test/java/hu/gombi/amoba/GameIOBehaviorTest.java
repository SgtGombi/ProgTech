package hu.gombi.amoba;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.io.TextBoardIO;
import hu.gombi.amoba.io.XmlBoardIO;
import hu.gombi.amoba.model.Board;

public class GameIOBehaviorTest {

    private final PrintStream originalOut = System.out;
    private final java.io.InputStream originalIn = System.in;

    @AfterEach
    void cleanup() throws Exception {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Files.deleteIfExists(Path.of("board.xml"));
        Files.deleteIfExists(Path.of("board.txt"));
        Files.deleteIfExists(Path.of("highscores.db"));
    }

    @Test
    void newGame_promptsForSizes_and_handlesInvalidInputs() throws Exception {
        // ensure no saved games exist
        Files.deleteIfExists(Path.of("board.xml"));
        Files.deleteIfExists(Path.of("board.txt"));

        // Provide invalid then valid rows/cols, then player name, then exit
        String inputs = "notnum\n3\n4\n10\n4\nPlayerName\nx\n";
        System.setIn(new ByteArrayInputStream(inputs.getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("Új játék létrehozása"));
        assertTrue(out.contains("Ervenytelen szam") || out.contains("Ervenytelen"));
    }

    @Test
    void illegalMove_isReported() throws Exception {
        // create and save a fresh board so Game loads it
        Board b = new Board(4, 4);
        TextBoardIO.save(b, Path.of("board.txt"), "P");

        // find the position of the existing X so an attempt to play there is illegal
        int rx = -1, cx = -1;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.cellAt(r, c) == hu.gombi.amoba.model.Cell.X) { rx = r; cx = c; break; }
            }
            if (rx != -1) break;
        }
        String cmd = String.valueOf((char)('a' + cx)) + (rx + 1);
        System.setIn(new ByteArrayInputStream((cmd + "\nx\n").getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("Illegal move"), "Attempting to play on occupied cell should print Illegal move");
    }

    @Test
    void resultsCommand_printsHighscores() throws Exception {
        // prepare a saved board so Game won't ask for sizes
        Board b = new Board(4, 4);
        TextBoardIO.save(b, Path.of("board.txt"), "P");

        // prepare highscores.db used by Game
        try (hu.gombi.amoba.db.Highscore repo = new hu.gombi.amoba.db.Highscore("jdbc:sqlite:highscores.db")) {
            repo.addWin("Zoli");
            repo.addWin("Anna");
            repo.addWin("Zoli");
        }

        System.setIn(new ByteArrayInputStream("r\nx\n".getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        // top should contain Zoli and Anna
        assertTrue(out.contains("Zoli") || out.contains("Anna"));
    }

    @Test
    void whenXmlPresent_gameLoadsXmlAndPrintsPlayer() throws Exception {
        Board b = new Board(4, 4);
        XmlBoardIO.save(b, Path.of("board.xml"), "Tester");

        // input: immediately exit
        ByteArrayInputStream in = new ByteArrayInputStream("x\n".getBytes());
        System.setIn(in);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("Mentett játék betöltve XML alapján"));
        assertTrue(out.contains("Játékos: Tester"));
    }

    @Test
    void saveCommand_writesBothTxtAndXmlFiles() throws Exception {
        Board b = new Board(4, 4);
        TextBoardIO.save(b, Path.of("board.txt"), "Saver");

        // perform save (m) then exit (x)
        ByteArrayInputStream in = new ByteArrayInputStream("m\nx\n".getBytes());
        System.setIn(in);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        // after run both files should exist
        assertTrue(Files.exists(Path.of("board.txt")), "board.txt should exist after save");
        assertTrue(Files.exists(Path.of("board.xml")), "board.xml should exist after save");

        String xml = Files.readString(Path.of("board.xml"));
        assertTrue(xml.contains("<player>"));
        String txt = Files.readString(Path.of("board.txt"));
        assertTrue(txt.startsWith("PLAYER:"));
    }

    @Test
    void humanMove_canWin_and_removesSaveFiles() throws Exception {
        // create a board where X has 3 in a row on row 0 and the 4th spot is free
        Board b = new Board(4, 4);
        // clear and set desired cells
        // place X at (0,0),(0,1),(0,2)
        b.makeMove(new hu.gombi.amoba.model.records.Move(new hu.gombi.amoba.model.records.Position(0,0), hu.gombi.amoba.model.Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new hu.gombi.amoba.model.records.Position(0,1), hu.gombi.amoba.model.Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new hu.gombi.amoba.model.records.Position(0,2), hu.gombi.amoba.model.Cell.X));
        // ensure (0,3) is empty

        XmlBoardIO.save(b, Path.of("board.xml"), "Winner");

        // human plays d1 (col d row 1) which is (0,3)
        ByteArrayInputStream in = new ByteArrayInputStream("d1\n".getBytes());
        System.setIn(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("X nyert"), "After the winning move, output should announce X nyert");
        // save files should be removed by game on win
        assertFalse(Files.exists(Path.of("board.xml")), "board.xml should be deleted after game end");
        assertFalse(Files.exists(Path.of("board.txt")), "board.txt should be deleted after game end");
    }
}
