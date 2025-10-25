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
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

// --- Ez az osztaly teszteli a Game osztaly IO viselkedeset
public class GameIOBehaviorTest {
    private final PrintStream originalOut = System.out;
    private final java.io.InputStream originalIn = System.in;

    // --- Minden teszt utan takaritja a fajlokat
    @AfterEach
    void cleanup() throws Exception {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Files.deleteIfExists(Path.of("board.xml"));
        Files.deleteIfExists(Path.of("board.txt"));
        Files.deleteIfExists(Path.of("highscores.db"));
    }

    // --- ez a teszt ellenorzi az uj jatek prompt-jait es ervenytelen bemeneteket
    @Test
    void newGame_promptsForSizes_and_handlesInvalidInputs() throws Exception {
        Files.deleteIfExists(Path.of("board.xml"));
        Files.deleteIfExists(Path.of("board.txt"));

        // --- ad ervenytelen majd ervenyes sorokat/oszlopokat, majd jatekos nevet, kilepes
        String inputs = "notnum\n3\n4\n10\n4\nPlayerName\nx\n";
        System.setIn(new ByteArrayInputStream(inputs.getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("Új játék létrehozása. (Nincs mentett játék.)"), "Output does not contain expected text. Full output: " + out);
        assertTrue(out.contains("Érvénytelen"), "Output does not contain invalid input message. Full output: " + out);
    }

    // --- ellenorzi, hogy az illegalis lepes jelentve van
    @Test
    void illegalMove_isReported() throws Exception {
        // --- letrehoz es ment egy friss tablat egy lepes nelkul
        Board b = new Board(4, 4);
        b.makeMove(new Move(new Position(0, 0), Cell.X));
        TextBoardIO.save(b, Path.of("board.txt"), "P");

        // --- megkeresi a letezo X poziciojat egy illegalis probalkozashoz
        int rx = -1, cx = -1;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.cellAt(r, c) == hu.gombi.amoba.model.Cell.X) {
                    rx = r;
                    cx = c;
                    break;
                }
            }
            if (rx != -1) {
                break;
            }
        }
        String cmd = String.valueOf((char) ('a' + cx)) + (rx + 1);
        System.setIn(new ByteArrayInputStream((cmd + "\nx\n").getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("Hibas lepes"), "Attempting to play on occupied cell should print Hibas lepes");
    }

    // --- ellenorzi a results parancsot es a highscore-okat
    @Test
    void resultsCommand_printsHighscores() throws Exception {
        // --- elokeszit egy mentett tablat
        Board b = new Board(4, 4);
        TextBoardIO.save(b, Path.of("board.txt"), "P");

        // --- elokeszit highscores.db-t
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
        // --- A top tartalmazza Zoli-t es Anna-t
        assertTrue(out.contains("Zoli") || out.contains("Anna"));
    }

    // --- Ez a teszt ellenorzi, hogy XML betolteskor kiirja a jatekost
    @Test
    void whenXmlPresent_gameLoadsXmlAndPrintsPlayer() throws Exception {
        Board b = new Board(4, 4);
        XmlBoardIO.save(b, Path.of("board.xml"), "Tester");

        // --- Bemenet: azonnal kilep
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

    // --- ez a teszt ellenorzi a save parancsot es a fajlok irasat
    @Test
    void saveCommand_writesBothTxtAndXmlFiles() throws Exception {
        Board b = new Board(4, 4);
        TextBoardIO.save(b, Path.of("board.txt"), "Saver");

        // --- Végrehajt save (m) majd kilep (x)
        ByteArrayInputStream in = new ByteArrayInputStream("m\nx\n".getBytes());
        System.setIn(in);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        // --- mindket fajl letezik
        assertTrue(Files.exists(Path.of("board.txt")), "board.txt should exist after save");
        assertTrue(Files.exists(Path.of("board.xml")), "board.xml should exist after save");

        String xml = Files.readString(Path.of("board.xml"));
        assertTrue(xml.contains("<player>"));
        String txt = Files.readString(Path.of("board.txt"));
        assertTrue(txt.startsWith("PLAYER:"));
    }

    // --- ez a teszt ellenorzi a human lepes nyereset es fajlok torleset
    @Test
    void humanMove_canWin_and_removesSaveFiles() throws Exception {
        // --- letrehoz egy tablat ahol X-nek 3 van sorban es a 4. szabad
        Board b = new Board(4, 4);
        b.makeMove(new Move(new Position(0,0), Cell.X));
        b.makeMove(new Move(new Position(0,1), Cell.X));
        b.makeMove(new Move(new Position(0,2), Cell.X));
        // --- Biztosítja, hogy (0,3) ures

        XmlBoardIO.save(b, Path.of("board.xml"), "Winner");

        // --- Human jatszik d1-et (col d row 1) ami (0,3)
        ByteArrayInputStream in = new ByteArrayInputStream("d1\n".getBytes());
        System.setIn(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Game g = new Game();
        g.start();

        String out = bos.toString();
        assertTrue(out.contains("X nyert"), "After the winning move, output should announce X nyert");
        // --- Save fajlok torolve a jatek vegen
        assertFalse(Files.exists(Path.of("board.xml")), "board.xml should be deleted after game end");
        assertFalse(Files.exists(Path.of("board.txt")), "board.txt should be deleted after game end");
    }
}
