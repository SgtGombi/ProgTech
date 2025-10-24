package hu.gombi.amoba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.records.Position;

// --- Ez az osztaly teszteli a Board osztalyt
public class BoardTest {
    // --- Ez a teszt ellenorzi a makeMove es cellAt metodusokat
    @Test
    public void testMakeMoveAndCellAt() {
        Board b = new Board(6, 6);
        // --- Megkeresi az elso ures cellat
        Position p = null;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.cellAt(r, c) == Cell.EMPTY) {
                    p = new Position(r, c);
                    break;
                }
            }
            if (p != null) {
                break;
            }
        }
        assertNotNull(p, "Should find an empty cell on a new board");
        assertEquals(Cell.EMPTY, b.cellAt(p.row(), p.col()));
        b.makeMove(new hu.gombi.amoba.model.records.Move(p, Cell.X));
        assertEquals(Cell.X, b.cellAt(p.row(), p.col()));
    }

    // --- Ez a teszt ellenorzi a legalMove metodust es a szomszedos cellakat
    @Test
    public void testLegalMoveBoundsAndNeighbor() {
        Board b = new Board(6, 6);
        Position out = new Position(-1, 0);
        assertFalse(b.legalMove(out));
        Position far = new Position(5, 5);
        // --- Kezdetben nincs szomszed, ezert nem legalis
        assertFalse(b.legalMove(far));
        // --- Hozzaad egy szomszedot
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(4, 4), Cell.X));
        assertTrue(b.hasNeighbor(5, 5));
        assertTrue(b.legalMove(far));
    }

    // --- Ez a teszt ellenorzi a vizszintes nyeresi feltetelt
    @Test
    public void testWinHorizontal() {
        Board b = new Board(6, 6);
        // --- Letrehoz negy X-et egymas mellett
        int row = 2;
        for (int c = 1; c <= 4; c++) {
            b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(row, c), Cell.X));
        }
        assertTrue(b.winCheck(Cell.X));
    }

    // --- Ez a teszt ellenorzi a fuggoleges nyeresi feltetelt
    @Test
    public void testWinVertical() {
        Board b = new Board(6, 6);
        int col = 3;
        for (int r = 0; r < 4; r++) {
            b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(r, col), Cell.O));
        }
        assertTrue(b.winCheck(Cell.O));
    }

    // --- Ez a teszt ellenorzi az atlos nyeresi feltetelt
    @Test
    public void testWinDiagonal() {
        Board b = new Board(6, 6);
        // --- Atlo lefele jobbra
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(0, 0), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(1, 1), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(2, 2), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(3, 3), Cell.X));
        assertTrue(b.winCheck(Cell.X));
    }

    // --- Ez a teszt ellenorzi az AI lepes legalis voltat
    @Test
    public void testRandomAIMoveIsLegal() {
        Board b = new Board(6, 6);
        // --- Kozepre es szomszedba helyez
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(2, 2), Cell.X));
        // --- Biztosítja, hogy vannak lehetseges lepesek
        Position ai = b.randomAImove();
        assertNotNull(ai);
        assertTrue(b.legalMove(ai));
    }

    // --- Ez a teszt ellenorzi az isFull metodust
    @Test
    public void testIsFull() {
        Board b = new Board(4, 4);
        assertFalse(b.isFull());
        // --- Feltoltjuk a tablat
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (b.cellAt(r, c) == Cell.EMPTY) {
                    b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(r, c), Cell.X));
                }
            }
        }
        assertTrue(b.isFull());
    }
}
