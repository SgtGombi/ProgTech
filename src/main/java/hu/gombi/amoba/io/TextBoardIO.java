package hu.gombi.amoba.io;

import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextBoardIO {

    // --- save fv.
    public static void save(Board board, Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) sb.append(board.cellAt(row, col).getCharacter());
            sb.append(System.lineSeparator());
        }
        Files.writeString(path, sb.toString());
    }

    // --- load fv.
    public static Board load(Path path) throws IOException {
        if (!Files.exists(path)) return null;
        var lines = Files.readAllLines(path);
        int rows = lines.size();
        int cols = lines.get(0).length();
        Board board = new Board(rows, cols);
        for (int row = 0; row <rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col <cols; col++) {
                char ch = line.charAt(col);
                if (ch=='x') board.makeMove(new Move(new Position(row, col), Cell.X));
                if (ch=='o') board.makeMove(new Move(new Position(row, col), Cell.O));
            }
        }
        return board;
    }
}
