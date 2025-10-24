package hu.gombi.amoba.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

public class XmlBoardIO {
    public static void save(Board b, Path p) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<board rows=\"").append(b.getRows()).append("\" cols=\"").append(b.getCols()).append("\">\n");
        for (int r=0;r<b.getRows();r++) {
            sb.append("  <row>\n    ");
            for (int c=0;c<b.getCols();c++) sb.append(b.cellAt(r,c).getCharacter());
            sb.append("\n  </row>\n");
        }
        sb.append("</board>\n");
        Files.writeString(p, sb.toString());
    }

    public static Board load(Path p) throws IOException {
        if (!Files.exists(p)) return null;
        var lines = Files.readAllLines(p);
        // naive parsing
        int rows = 0, cols = 0; boolean inRow=false; Board b=null; int r=0;
        for (String line: lines) {
            line = line.trim();
            if (line.startsWith("<board")) {
                String rr = line.replaceAll(".*rows=\\\"(\\d+)\\\".*","$1");
                String cc = line.replaceAll(".*cols=\\\"(\\d+)\\\".*","$1");
                rows = Integer.parseInt(rr); cols = Integer.parseInt(cc);
                b = new Board(rows, cols);
            } else if (line.startsWith("<row>")) { inRow=true; }
            else if (line.startsWith("</row>")) { inRow=false; r++; }
            else if (inRow) {
                for (int c=0;c<line.length();c++) {
                    char ch = line.charAt(c);
                    if (ch=='x') b.makeMove(new Move(new Position(r,c), Cell.X));
                    if (ch=='o') b.makeMove(new Move(new Position(r,c), Cell.O));
                }
            }
        }
        return b;
    }
}
