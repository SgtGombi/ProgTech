package hu.gombi.amoba.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import hu.gombi.amoba.model.Board;
import hu.gombi.amoba.model.Cell;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

public class XmlBoardIO {

    public static record SavedGame(Board board, String playerName) {}

    // --- save fv xml-be, player nevet is menti
    public static void save(Board b, Path p, String playerName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<game>\n");
        sb.append("  <meta>\n");
        sb.append("    <rows>").append(b.getRows()).append("</rows>\n");
        sb.append("    <cols>").append(b.getCols()).append("</cols>\n");
        sb.append("    <player>").append(escapeXml(playerName)).append("</player>\n");
        sb.append("  </meta>\n");
        sb.append("  <board>\n");
        for (int r=0;r<b.getRows();r++) {
            sb.append("    <row>");
            for (int c=0;c<b.getCols();c++) sb.append(b.cellAt(r,c).getCharacter());
            sb.append("</row>\n");
        }
        sb.append("  </board>\n");
        sb.append("</game>\n");
        Files.writeString(p, sb.toString());
    }

    // --- load fv.: betolti a savedgame altal mentett jatekot
    public static SavedGame load(Path p) throws IOException {
        if (!Files.exists(p)) return null;
        List<String> lines = Files.readAllLines(p);
        int rows = 0, cols = 0; boolean inRow=false; Board b=null; int r=0; String playerName = "";
        for (String rawLine: lines) {
            String line = rawLine.trim();
            if (line.startsWith("<rows>")) {
                String content = line.replaceAll("<rows>(\\d+)</rows>", "$1");
                rows = Integer.parseInt(content);
            } else if (line.startsWith("<cols>")) {
                String content = line.replaceAll("<cols>(\\d+)</cols>", "$1");
                cols = Integer.parseInt(content);
            } else if (line.startsWith("<player>")) {
                playerName = line.replaceAll("<player>(.*)</player>", "$1");
            } else if (line.startsWith("<row>")) { inRow = true; String rowContent = line.replaceAll("<row>(.*)</row>", "$1");
                if (b == null && rows>0 && cols>0) b = new Board(rows, cols);
                // handle single-line row content
                for (int c=0;c<rowContent.length();c++) {
                    char ch = rowContent.charAt(c);
                    if (ch=='x') b.makeMove(new Move(new Position(r,c), Cell.X));
                    if (ch=='o') b.makeMove(new Move(new Position(r,c), Cell.O));
                }
                r++;
                inRow = false;
            } else if (inRow) {
                for (int c=0;c<line.length();c++) {
                    char ch = line.charAt(c);
                    if (ch=='x') b.makeMove(new Move(new Position(r,c), Cell.X));
                    if (ch=='o') b.makeMove(new Move(new Position(r,c), Cell.O));
                }
                r++;
            }
        }
        if (b == null && rows>0 && cols>0) b = new Board(rows, cols);
        return new SavedGame(b, playerName);
    }
    // --- xml spec karakterek escapelésére
    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
