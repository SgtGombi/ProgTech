package hu.gombi.amoba;

import hu.gombi.amoba.db.Highscore;
import hu.gombi.amoba.io.TextBoardIO;
import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

public class Game {
    private Board board;
    private final Scanner sc = new Scanner(System.in);

    public Game() {
        Board loaded = null;
        try {
            loaded = TextBoardIO.load(Path.of("board.txt"));
        } catch (Exception ignored) {
        }

        if (loaded == null) {
            System.out.println("Add meg a tábla méretét");

            int rows = 0;
            int cols = 0;

            // --- sor számának bekérése, user input ellenőrzés
            while (rows < 4 || rows > 25) {
                System.out.print("Sorok száma: ");
                if (sc.hasNextInt()) {
                    rows = sc.nextInt();
                    if (rows < 4 || rows > 25) {
                        System.out.println("A sorok száma 4 és 25 között lehet.");
                    }
                } else {
                    System.out.println("Érvénytelen szám.");
                    sc.next(); // hibás input eltávolítása
                }
            }

            // --- oszlop számának bekérése, user input ellenőrzés
            while (cols < 4 || cols > rows) {
                System.out.print("Oszlopok száma: ");
                if (sc.hasNextInt()) {
                    cols = sc.nextInt();
                    if (cols < 4) {
                        System.out.println("Az oszlopok száma legalább 4 lehet.");
                    } else if (cols > rows) {
                        System.out.println("Az oszlopok száma nem lehet nagyobb, mint a sorok száma.");
                    }
                } else {
                    System.out.println("Érvénytelen szám.");
                    sc.next(); // hibás input eltávolítása
                }
            }

            this.board = new Board(rows, cols);
        } else {
            this.board = loaded;
        }
    }

    // --- start method
    public void start () {
        System.out.print("Player name: ");
        String name = sc.nextLine();
        while (true) {
            board.print();
            System.out.print("Adj meg egy lépést! (pl: a3) vagy válassz: 'save'/'exit'/'score': ");
            String cmd = sc.nextLine();
            if (cmd.equals("save")) {
                try {
                    TextBoardIO.save(board, Path.of("board.txt"));
                } catch (Exception ignored) {
                }
                continue;
            }
            if (cmd.equals("score")) {
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    Map<String, Integer> top = repo.top();
                    top.forEach((k, v) -> System.out.println(k + " " + v));
                } catch (Exception ignored) {
                }
                continue;
            }
            if (cmd.equals("exit")) return;
            // human move -> map a1 to position and validate
            try {
                int col = cmd.charAt(0) - 'a';
                int row = Integer.parseInt(cmd.substring(1)) - 1;
                Position p = new Position(row, col);
                if (!board.legalMove(p)) {
                    System.out.println("Illegal move");
                    continue;
                }
                board.makeMove(new Move(p, Cell.X));
            } catch (Exception e) {
                System.out.println("Invalid input");
                continue;
            }
            if (board.winCheck(Cell.X)) {
                board.print();
                System.out.println("X wins");
                try (Highscore repo = new Highscore("jdbc:sqlite:highscores.db")) {
                    repo.addWin(name);
                } catch (Exception ignored) {
                }
                return;
            }
            Position ai = board.randomAImove();
            if (ai != null) board.makeMove(new Move(ai, Cell.O));
            if (board.winCheck(Cell.O)) {
                board.print();
                System.out.println("O wins");
                return;
            }
        }
    }
}