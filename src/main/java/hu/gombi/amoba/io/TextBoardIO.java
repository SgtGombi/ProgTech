package hu.gombi.amoba.io;

import java.nio.file.Files;
import java.nio.file.Path;

import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

public class TextBoardIO {

    // --- segedosztaly ami mentett board+jatekos nevet tartalmaz ---
    public static record SavedGame(hu.gombi.amoba.model.Board board, String playerName) {}

    // --- txt mentes, jatekosnev a tetejen. ---
    public static void save(Board board, Path path, String playerName) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAYER:").append(playerName).append(System.lineSeparator());
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) sb.append(board.cellAt(row, col).getCharacter());
            sb.append(System.lineSeparator());
        }
        Files.writeString(path, sb.toString());
    }

    // --- load fv.: betolti a savedgame altal mentett jatekot
    public static SavedGame load(Path path) throws java.io.IOException {
        if (!Files.exists(path)) return null;
        var lines = Files.readAllLines(path);
        if (lines.isEmpty()) return null;
        String first = lines.get(0);
        String playerName = "";
        int startRow = 0;
        if (first.startsWith("PLAYER:")) {
            playerName = first.substring("PLAYER:".length());
            startRow = 1;
        }
        int rows = lines.size() - startRow;
        if (rows <= 0) return null;
        int cols = lines.get(startRow).length();
        Board board = new Board(rows, cols, false);
        for (int r = 0; r < rows; r++) {
            String line = lines.get(startRow + r);
            for (int c = 0; c < cols; c++) {
                char ch = line.charAt(c);
                if (ch == 'x') board.makeMove(new Move(new Position(r, c), Cell.X));
                if (ch == 'o') board.makeMove(new Move(new Position(r, c), Cell.O));
            }
        }
        return new SavedGame(board, playerName);
    }
}
