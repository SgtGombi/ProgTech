package hu.gombi.amoba.model;

import java.util.Random;

public class Board {
    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Board(int rows,int cols) {
        // --- FELADATLEIRAS: 4 <=M <= N <=25
        // --- kulon kezeljuk a hibas tablameretet, pontos hibauzenet a usernek
        if(cols<4 || cols > 25) throw new IllegalArgumentException("A sorok számának 4 és 25 között kell lennie!");
        if(rows<4 || rows > 25) throw new IllegalArgumentException("Az oszlopook számának 4 és 25 között kell lennie!");
        if(cols>rows) throw new IllegalArgumentException("Az oszlopok száma nem lehet nagyobb a sorok számánál!");

        // --- konstruktor
        this.rows=rows;
        this.cols=cols;
        this.cells=new Cell[rows][cols];

        // --- minden mezo ures kezdetben ('.')
        for(int i=0;i<rows;i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = Cell.EMPTY;
            }
        }
        // --- FELADATLEIRAS: palya EGYIK kozepso mezojere tesz automatikusan
        // --- mivel nem pont kozep: kiszamolom a kozepet (rows/2) kivonok belole egyet, majd random 0-1-3
        if(rows%2==0) int xStartRow = rows/2-1+random.nextInt(3); else int xStartRow = rows/2+random.nextInt(3);
        if(cols%2==0) int xStartCol = cols/2-1+random.nextInt(3); else int xStartCol = cols/2+random.nextInt(3);
        // FOLYT KÖV HOLNAP: valtozok es randint hiba

    }



}