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
            //
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
                if (this.playerName != null && !this.playerName.isBlank()) System.out.println("Játékos: " + this.playerName);
                LOGGER.info("Loaded saved game for player={}", this.playerName);
            } else {
                // --- nincs mentett jatek -> uj tabla
                System.out.println("Új játék létrehozása. (Nincs mentett játék.)");
                int cols = readIntInRange("Oszlopok száma (4-25): ", 4, 25);
                int rows = readIntInRange("Sorok száma (" + cols + "-25): ", cols, 25);
                this.board = new Board(rows, cols);
                // empty board a feladatleírás szerint.
                System.out.println("Feladat: A játék kezdetben üres.");
                Board empty = new Board(rows, cols, false);
                empty.print();

                // --- logika miatt ha rogton x kezd, kell ra valasz az AI-tol is o-val.
                Position immediateAi = this.board.randomAImove();
                if (immediateAi != null) this.board.makeMove(new Move(immediateAi, Cell.O));
                System.out.print("Játékos neve: ");
                this.playerName = scanner.nextLine();
                LOGGER.info("Starting new game: rows={}, cols={}, player={}", rows, cols, this.playerName);
            }
        }
    }

    // --- start: jatek inditasa
    public void start() {
        playGame(this.board, this.playerName);
    }

    // --- jatek vegrehajtasa; visszater true-val ha a felhasznalo az exit parancsot adta vissza
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
                    XmlBoardIO.save(board, Path.of("board.xml"), playerName);
                    xmlOk = true;
                } catch (IOException ignored) {
                    //
                }
                if (txtOk && xmlOk) System.out.println("Sikeresen elmentve TXT-be és XML-be.");
                else System.out.println("Mentés sikertelen.");
                continue;
            }
            if (cmd.equals("r")) {
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    System.out.println("Név | Győzelmek");
                    repo.top().forEach((k, v) -> System.out.println(k + " | " + v));
                } catch (Exception e) {
                    System.out.println("Eredmények betöltése sikertelen.");
                }
                continue;
            }
            if (cmd.equals("x")) {
                return true; // kilep a programbol
            }

            // --- user lepes
            try {
                int col = cmd.charAt(0) - 'a';
                int row = Integer.parseInt(cmd.substring(1)) - 1;
                Position pos = new Position(row, col);
                if (!board.legalMove(pos)) {
                    System.out.println("Hibás lépés");
                    continue;
                }
                board.makeMove(new Move(pos, Cell.X));
            } catch (RuntimeException e) {
                System.out.println("Hibás input, próbáld újra. (pl: B3)");
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
                // toroljuk a mentettet ha vegigment a meccs
                try { Files.deleteIfExists(Path.of("board.xml")); } catch (IOException ignored) {}
                try { Files.deleteIfExists(Path.of("board.txt")); } catch (IOException ignored) {}
                return true; // kilep a programbol
            }

            // --- Ha nincs tobb EMPTY mezo, akkor dontetlen es kilep
            if (board.isFull()) {
                board.print();
                System.out.println("Döntetlen");
                try { Files.deleteIfExists(Path.of("board.xml")); } catch (IOException ignored) {}
                try { Files.deleteIfExists(Path.of("board.txt")); } catch (IOException ignored) {}
                return true;
            }
        }
    }
    // --- input helper sor/oszlop letrehozaskor, ellenorzi az ertekeket.
    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Érvénytelen, maradjon " + min + " és " + max + " között.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Érvénytelen szám.");
            }
        }
    }
}