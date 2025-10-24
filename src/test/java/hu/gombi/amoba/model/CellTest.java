package hu.gombi.amoba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

// --- Ez az osztaly teszteli a Cell enumot
public class CellTest {
    // --- Ez a teszt ellenorzi a fromCharacter metodust
    @Test
    public void testFromCharacter() {
        assertEquals(Cell.X, Cell.fromCharacter('x'));
        assertEquals(Cell.O, Cell.fromCharacter('o'));
        assertEquals(Cell.EMPTY, Cell.fromCharacter('.'));
        assertEquals(Cell.EMPTY, Cell.fromCharacter('a'));
    }
}