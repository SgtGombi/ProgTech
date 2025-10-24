package hu.gombi.amoba;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import hu.gombi.amoba.db.Highscore;
import hu.gombi.amoba.io.TextBoardIO;
import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

public class Game {
    private Board board;
    private final Scanner sc = new Scanner(System.in);
    // --- mentes torlese ha befejezett betoltott jatek
    private boolean loadedFromFile = false;

    public Game() {
        // konstruktor csak scanner-t tart, a menu kezeli a jatekokat
    }

    // --- start: fo menu es kezelok
    public void start() {
        while (true) {
            System.out.println("Főmenü: (u) Új játék | (j) Játék betöltése | (e) Eredmények | (x) Kilépés");
            System.out.print("Válassz: ");
            String cmd = sc.nextLine().trim().toLowerCase();
            if (cmd.equals("u")) {
                // --- uj jatek
                Board newBoard = createNewBoard();
                if (newBoard == null) continue;
                loadedFromFile = false;
                System.out.print("Player name: ");
                String playerName = sc.nextLine();
                boolean exitRequested = playGame(newBoard, playerName);
                if (exitRequested) return;
            } else if (cmd.equals("j")) {
                // --- betoltes
                TextBoardIO.SavedGame saved = null;
                try {
                    saved = TextBoardIO.load(Path.of("board.txt"));
                } catch (Exception ignored) {
                }
                if (saved == null) {
                    System.out.println("Nincs mentett jatek.");
                    continue;
                }
                loadedFromFile = true;
                String playerName = saved.playerName();
                boolean exitRequested = playGame(saved.board(), playerName);
                if (exitRequested) return;
            } else if (cmd.equals("e")) {
                // --- eredmenyek
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    Map<String, Integer> top = repo.top();
                    top.forEach((k, v) -> System.out.println(k + " " + v));
                } catch (Exception ignored) {
                }
            } else if (cmd.equals("x")) {
                return; // kilepes a programbol
            } else {
                System.out.println("Ismeretlen parancs.");
            }
        }
    }

    // --- jatek vegrehajtasa; visszater true-val ha a felhasznalo az exit parancsot adta ossze (kilep a programbol)
    private boolean playGame(Board boardToPlay, String playerName) {
        this.board = boardToPlay;
        while (true) {
            board.print();
            System.out.print("Adj meg egy lépést! (pl: a3) vagy válassz: (m) Mentés  (f) Főmenü: ");
            String cmd = sc.nextLine().trim().toLowerCase();
            if (cmd.equals("m")) {
                try {
                    TextBoardIO.save(board, Path.of("board.txt"), playerName);
                    System.out.println("Sikeresen elmentve.");
                } catch (Exception ignored) {
                    System.out.println("Mentés sikertelen.");
                }
                continue;
            }
            if (cmd.equals("f")) {
                return false; // vissza a fomenube
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
            } catch (Exception e) {
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
                if (loadedFromFile) {
                    try {
                        Files.deleteIfExists(Path.of("board.txt"));
                    } catch (Exception ignored) {
                    }
                }
                return false; // vissza a fomenube
            }

            Position aiMove = board.randomAImove();
            if (aiMove != null) board.makeMove(new Move(aiMove, Cell.O));

            if (board.winCheck(Cell.O)) {
                board.print();
                System.out.println("O nyert");
                if (loadedFromFile) {
                    try {
                        Files.deleteIfExists(Path.of("board.txt"));
                    } catch (Exception ignored) {
                    }
                }
                return false; // vissza a fomenube
            }
        }
    }

    // --- seged: uj tabla letrehozasa a felhasznalotol bekerve
    private Board createNewBoard() {
        int rows = readIntInRange("Sorok szama: ", 4, 25);
        if (rows == -1) return null;
        int cols = readIntInRange("Oszlopok szama: ", 4, rows);
        if (cols == -1) return null;
        return new Board(rows, cols);
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
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