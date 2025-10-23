package hu.gombi.amoba;

import hu.gombi.amoba.model.Board;

import java.util.Scanner;

public class Game {
    private Board board;
    private final Scanner sc = new Scanner(System.in);

    public Game() {
        System.out.println("Add meg a tábla méretét");

        int rows=0;
        int cols=0;
        // --- sor szamanak bekerese, user input ellenorzes
        while (rows < 4 || rows > 25) {
            System.out.print("Sorok száma: ");
            if (sc.hasNextInt()) {
                rows = sc.nextInt();
                if (rows < 4 || rows > 25) {
                    System.out.println("A sorok száma 4 és 25 között lehet.");
                }
            } else {
                System.out.println("Érvénytelen szám.");
                sc.next(); // hibás input eltávolítása
            }
        }
        // --- oszlop szamanak bekerese, user input ellenorzes
        while (cols < 4 || cols > rows) {
            System.out.print("Oszlopok száma: ");
            if (sc.hasNextInt()) {
                cols = sc.nextInt();
                if (cols < 4) {
                    System.out.println("Az oszlopok száma legalább 4 lehet.");
                } else if (cols > rows) {
                    System.out.println("Az oszlopok száma nem lehet nagyobb, mint a sorok száma.");
                }
            } else {
                System.out.println("Érvénytelen szám.");
                sc.next();
            }
        }
        this.board = new Board(rows, cols);
    }
    // start method kesobb ide
}
