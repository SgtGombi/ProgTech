package hu.gombi.amoba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hu.gombi.amoba.model.records.Position;

public class BoardTest {

    @Test
    public void testMakeMoveAndCellAt() {
        Board b = new Board(6,6);
        // find any empty cell to avoid randomness affecting the center placement
        Position p = null;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.cellAt(r, c) == Cell.EMPTY) {
                    p = new Position(r, c);
                    break;
                }
            }
            if (p != null) break;
        }
        assertNotNull(p, "Should find an empty cell on a new board");
        assertEquals(Cell.EMPTY, b.cellAt(p.row(), p.col()));
        b.makeMove(new hu.gombi.amoba.model.records.Move(p, Cell.X));
        assertEquals(Cell.X, b.cellAt(p.row(), p.col()));
    }

    @Test
    public void testLegalMoveBoundsAndNeighbor() {
        Board b = new Board(6,6);
        Position out = new Position(-1,0);
        assertFalse(b.legalMove(out));
        Position far = new Position(5,5);
        // initially no neighbors -> illegal
        assertFalse(b.legalMove(far));
        // put a neighbor
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(4,4), Cell.X));
        assertTrue(b.hasNeighbor(5,5));
        assertTrue(b.legalMove(far));
    }

    @Test
    public void testWinHorizontal() {
        Board b = new Board(6,6);
        // create four in a row for X
        int row = 2;
        for (int c=1;c<=4;c++) b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(row,c), Cell.X));
        assertTrue(b.winCheck(Cell.X));
    }

    @Test
    public void testWinVertical() {
        Board b = new Board(6,6);
        int col = 3;
        for (int r=0;r<4;r++) b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(r,col), Cell.O));
        assertTrue(b.winCheck(Cell.O));
    }

    @Test
    public void testWinDiagonal() {
        Board b = new Board(6,6);
        // diag down-right
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(0,0), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(1,1), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(2,2), Cell.X));
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(3,3), Cell.X));
        assertTrue(b.winCheck(Cell.X));
    }

    @Test
    public void testRandomAIMoveIsLegal() {
        Board b = new Board(6,6);
        // seed center and neighbor
        b.makeMove(new hu.gombi.amoba.model.records.Move(new Position(2,2), Cell.X));
        // ensure there are possible moves
        Position ai = b.randomAImove();
        assertNotNull(ai);
        assertTrue(b.legalMove(ai));
    }
}
