package hu.gombi.amoba.model;
import hu.gombi.amoba.model.records.Move;
import hu.gombi.amoba.model.records.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Board(int rows, int cols) {
        // --- FELADATLEIRAS: 4 <=M <= N <=25
        // --- kulon kezeljuk a hibas tablameretet, pontos hibauzenet a usernek
        if (cols < 4 || cols > 25) throw new IllegalArgumentException("A sorok számának 4 és 25 között kell lennie!");
        if (rows < 4 || rows > 25)
            throw new IllegalArgumentException("Az oszlopook számának 4 és 25 között kell lennie!");
        if (cols > rows) throw new IllegalArgumentException("Az oszlopok száma nem lehet nagyobb a sorok számánál!");

        // --- konstruktor
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];

        // --- minden mezo ures kezdetben ('.')
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = Cell.EMPTY;
            }
        }
        // --- FELADATLEIRAS: palya EGYIK kozepso mezojere tesz automatikusan
        // --- páros/páratlan esetén másképp számolom a középső zónát
        Random random = new Random();
        int xStartRow, xStartCol;
        if (rows % 2 == 0) xStartRow = rows / 2 - 1 + random.nextInt(3);
        else xStartRow = rows / 2 + random.nextInt(3);
        if (cols % 2 == 0) xStartCol = cols / 2 - 1 + random.nextInt(3);
        else xStartCol = cols / 2 + random.nextInt(3);
        cells[xStartRow][xStartCol] = Cell.X;
    }

    // --- GETTER, visszaadja a sorokat oszlopokat es hogy adott mezonek mi az allapota
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell cellAt(int r, int c) {
        return cells[r][c];
    }

    // --- clean code: a lepes es az ellenorzesek kulonvalasztva
    // --- lepes vegrehajtasa
    public void MakeMove(Move move) {
        Position pos = move.pos();
        cells[pos.row()][pos.col()] = move.symbol();
    }

    // --- szomszedos mezok ellenorzese: Tobb metodushoz is kell.
    public boolean hasNeighbor(int row, int col) {
        for (int nextRows = -1; nextRows < 2; nextRows++) {
            for (int nextCols = -1; nextCols < 2; nextCols++) {
                if (nextCols == 0 && nextRows == 0) continue;
                int neighborRow = row + nextRows;
                int neighborCol = col + nextCols;
                if (neighborRow >= 0 && neighborRow < rows && neighborCol >= 0 && neighborCol < cols) {
                    if (cells[neighborRow][neighborCol] != Cell.EMPTY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- szabalyos-e a lepes
    public boolean legalMove(Position pos) {
        int row = pos.row();
        int col = pos.col();
        // ha a sor/oszlop nem letezik vagy a mezo foglalt: FALSE
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        if (cells[row][col] != Cell.EMPTY) return false;
        return hasNeighbor(row, col);
    }

    // --- ures cellak kigyujtese a random AI lepeshez + lepes
    public Position randomAImove() {
        List<Position> possibleMoves = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (cells[row][col] == Cell.EMPTY && hasNeighbor(row, col)) {
                    possibleMoves.add(new Position(row, col));
                }
            }
        }
        if(possibleMoves.isEmpty()) return null;
        return possibleMoves.get(new Random().nextInt(possibleMoves.size()));
    }

    // --- win ellenorzes
    public boolean WinCheck(Cell symbol) {
        char character = symbol.getCharacter();
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (cells[row][col].getCharacter() != character) continue;
                for (int[] dir : directions) {
                    int count = 0;
                    int r = row, c = col;
                    while (r >= 0 && r < rows && c >= 0 && c < cols && cells[r][c].getCharacter() == character) {
                        count++;
                        r += dir[0];
                        c += dir[1];
                    }
                    if (count >= 5) return true;
                }
            }
        }
        return false;
    }
    // --- board kiiratása
    public void print() {
        // --- oszlopnev betuk kiiratasa
        System.out.print("  ");
        for(int col=0;col<cols;col++) {
            char letter = (char) ('a'+col);
            System.out.print(letter+" ");
        }
        System.out.println();

        // --- sorszamok kiiratasa
        for(int row=0;row<rows;row++) {
            System.out.print((row+1)+" ");
            for(int col=0;col<cols;col++) {
                System.out.print(cells[row][col].getCharacter()+" ");
            }
            System.out.println();
        }
    }
}