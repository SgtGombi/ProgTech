package hu.gombi.amoba.model;

public enum Cell {
    // --- 3 lehetséges allapot enumon belul
    EMPTY('.'), X('x'), O('o');
    private final char character;

    // --- konstruktor
    Cell(char character) {this.character=character;}

    // --- getter, mit is tartalmaz a cella amit kiirunk
    public char getCharacter() {
        return character;
    }
    // --- mentes betolteshez kell, static mert nem peldanyfuggo
    public static Cell fromCharacter(char c) {
        if (c == 'x') return X;
        if (c == 'o') return O;
        return EMPTY;
    }
}
