package hu.gombi.amoba;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.gombi.amoba.db.Highscore;
import hu.gombi.amoba.io.TextBoardIO;
import hu.gombi.amoba.io.XmlBoardIO;
import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

public class Game {
    private Board board;
    private final Scanner scanner = new Scanner(System.in);
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);
    private String playerName = "";

    public Game() {
        // Indulaskor probaljuk betolteni: eloszor XML-t (board.xml), ha nincs akkor a board.txt-et
        TextBoardIO.SavedGame saved = null;
        try {
            var xmlSaved = XmlBoardIO.load(Path.of("board.xml"));
            if (xmlSaved != null && xmlSaved.board() != null) {
                this.board = xmlSaved.board();
                this.playerName = xmlSaved.playerName();
                System.out.println("Mentett játék betöltve XML alapján");
                if (this.playerName != null && !this.playerName.isBlank()) System.out.println("Játékos: " + this.playerName);
                LOGGER.info("Loaded saved game from XML, player={}", this.playerName);
            }
        } catch (IOException ignored) {
            // ignore load problems
        }

        if (this.board == null) {
            try {
                saved = TextBoardIO.load(Path.of("board.txt"));
            } catch (IOException ignored) {
            }
            if (saved != null) {
                this.board = saved.board();
                this.playerName = saved.playerName();
                System.out.println("Mentett játék betöltve TXT alapján");
                LOGGER.info("Loaded saved game for player={}", this.playerName);
            } else {
                // nincs mentett jatek -> uj tabla
                System.out.println("Új játék létrehozása. (Nincs mentett játék.)");
                int rows = readIntInRange("Sorok száma: ", 4, 25);
                int cols = readIntInRange("Oszlopok száma: ", 4, rows);
                this.board = new Board(rows, cols);
                // After the automatic X is placed by the Board constructor,
                // immediately make one AI move so the human sees both X and O
                Position immediateAi = this.board.randomAImove();
                if (immediateAi != null) this.board.makeMove(new Move(immediateAi, Cell.O));
                System.out.print("Játékos neve: ");
                this.playerName = scanner.nextLine();
                LOGGER.info("Starting new game: rows={}, cols={}, player={}", rows, cols, this.playerName);
            }
        }
    }

    // --- start: egybol jatek inditasa (nincs fo menu)
    public void start() {
        // elinditjuk a jatekot a betoltott vagy uj palyan
        boolean exit = playGame(this.board, this.playerName);
        if (exit) return;
    }

    // --- jatek vegrehajtasa; visszater true-val ha a felhasznalo az exit parancsot adta ossze (kilep a programbol)
    private boolean playGame(Board boardToPlay, String playerName) {
        this.board = boardToPlay;
        while (true) {
            board.print();
            System.out.print("Adj meg egy lépést! (pl: a3) vagy válassz: (m) Mentés (xml/txt)  (r) Eredmenyek  (x) Kilépés: ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            if (cmd.equals("m")) {
                boolean txtOk = false;
                boolean xmlOk = false;
                try {
                    TextBoardIO.save(board, Path.of("board.txt"), playerName);
                    txtOk = true;
                } catch (IOException ignored) {
                    // txt save failed
                }
                try {
                    XmlBoardIO.save(board, Path.of("board.xml"));
                    xmlOk = true;
                } catch (IOException ignored) {
                    // xml save failed
                }
                if (txtOk && xmlOk) System.out.println("Sikeresen elmentve TXT-be és XML-be.");
                else if (txtOk) System.out.println("Sikeresen elmentve TXT-be, XML mentés sikertelen.");
                else if (xmlOk) System.out.println("Sikeresen elmentve XML-be, TXT mentés sikertelen.");
                else System.out.println("Mentés sikertelen mindkét formátumban.");
                continue;
            }
            if (cmd.equals("r")) {
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    repo.top().forEach((k, v) -> System.out.println(k + " " + v));
                } catch (Exception ignored) {
                    System.out.println("Eredmenyek betoltese sikertelen.");
                }
                continue;
            }
            if (cmd.equals("x")) {
                return true; // kilep a programbol
            }

            // human move
            try {
                int col = cmd.charAt(0) - 'a';
                int row = Integer.parseInt(cmd.substring(1)) - 1;
                Position pos = new Position(row, col);
                if (!board.legalMove(pos)) {
                    System.out.println("Illegal move");
                    continue;
                }
                board.makeMove(new Move(pos, Cell.X));
            } catch (RuntimeException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (board.winCheck(Cell.X)) {
                board.print();
                System.out.println("X nyert");
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    repo.addWin(playerName);
                } catch (Exception ignored) {
                }
                // remove any saved state files so the finished match cannot be resumed
                try { Files.deleteIfExists(Path.of("board.xml")); } catch (IOException ignored) {}
                try { Files.deleteIfExists(Path.of("board.txt")); } catch (IOException ignored) {}
                return true; // kilep a programbol
            }

            Position aiMove = board.randomAImove();
            if (aiMove != null) board.makeMove(new Move(aiMove, Cell.O));

            if (board.winCheck(Cell.O)) {
                board.print();
                System.out.println("O nyert");
                // remove any saved state files so the finished match cannot be resumed
                try { Files.deleteIfExists(Path.of("board.xml")); } catch (IOException ignored) {}
                try { Files.deleteIfExists(Path.of("board.txt")); } catch (IOException ignored) {}
                return true; // kilep a programbol
            }
        }
    }
    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Ervenytelen, maradjon " + min + " es " + max + " kozott.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ervenytelen szam.");
            }
        }
    }
}