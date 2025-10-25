# Amőba Játék

## Futtatás:
- Futtatáshoz git clone, java és maven szükséges.
- mvn clean install
- java -jar .\target\amoba-{VERZIÓ SZÁMA}-{VERZIÓ NEVE}-jar-with-dependencies.jar
- Jelenleg: java -jar .\target\amoba-1.1-BACON-jar-with-dependencies.jar

## FONTOS MEGJEGYZÉSEK:
- Nem tiszta a feladatleírás: a legkisebb tábla 4x4 lehet, szövegben elsőnek az van megadva hogy 5-öt kell csatlakoztatni, majd alatta nem sokkal hogy "Connect 4". Ha 5-el csinálom, a 4x4-es táblán csak keresztbe lehet nyerni, kétlem hogy ez lett volna a cél.
KÖVETKEZTETÉS: 4-es connect a játékszabályom, bármikor módosítható 5-re.

- Mentés: Kéri a feladat a mentést txt-be mint alapfunkció. De plusz pontért kéri az XML-be mentést is. Itt nem derül ki a szövegből, hogy az az alap txt mentés funkció cseréjét jelenti-e.
KÖVETKEZTETÉS: Megcsináltam az XML-be és TXT-be mentést is, alapvetően xml-el dolgozik, ha azzal valami problémás, akkor a txt-t veszi elő.

- Az elvárás hogy automata X lépéssel kezdjünk, alatta pedig az szerepel, hogy ha nincs mentett játékállás, akkor "üres" pályáról kezdjünk. Gondolom itt az alapértelmezett kell.
KÖVETKEZTETÉS: Mivel szigorúan automatikus x kezdés van, aztán meg a gép jön, így új játék kezdetekor már egy X-O párt látunk, és innen kezdődik.

- Mivel még sosem használtam Java/Maven-t (php/laravel/python/kevés node.js amivel leginkább dolgozom) így nyilván használtam az értelmezésre, és tervezésre AI-t, magamtól nyilván nem ismertem a pluginokat, dependencies-eket.

## Projekt történet:
### Részletes történet a GITHUB-on!


## Mappaszerkezet
- amoba/
  - pom.xml
  - README.md
  - logs/
  - src/
    - main/
      - java/
        - hu/
          - gombi/
            - amoba/
              - Amoba.java
              - Game.java
              - db/
                - Highscore.java
              - io/
                - TextBoardIO.java
                - XmlBoardIO.java
              - model/
                - Board.java
                - Cell.java
                - records/
                  - Move.java
                  - Player.java
                  - Position.java
    - test/
      - java/
        - hu/
          - gombi/
            - amoba/
              - AmobaMainTest.java
              - GameIOBehaviorTest.java
              - db/
                - HighscoreMockitoTest.java
                - HighscoreTest.java
              - io/
                - TextBoardIOExtraTest.java
                - TextBoardIOTest.java
                - XmlBoardIOExtraTest.java
              - model/
                - BoardTest.java
                - CellTest.java
  - target/
    └── ...

## Fájlok leírása

### Amoba.java
- Ez az entry point osztály.
- Metódusok:
  - main(String[] args): létrehoz egy Game példányt és elindítja.

### Game.java
- Ez az osztály kezeli a játék logikáját és felhasználói interakciót.
- Metódusok:
  - Game(): konstruktor, betölti a mentett játékot ha van.
  - start(): elindítja a játékot.
  - playGame(Board, String): végrehajtja a játék ciklust.
  - readIntInRange(String, int, int): beolvas egy számot a tartományon belül.

### Highscore.java
- Ez az osztály kezeli a highscore adatbázist.
- Metódusok:
  - Highscore(String): konstruktor, csatlakozik az adatbázishoz.
  - addWin(String): hozzáad egy győzelmet a játékosnak.
  - top(): visszaadja a top lista map-et.

### TextBoardIO.java
- Ez az osztály kezeli a TXT formátumú mentés/betöltés.
- Metódusok:
  - save(Board, Path, String): menti a táblát és játékost TXT-be.
  - load(Path): betölti a SavedGame-et TXT-ból.

### XmlBoardIO.java
- Ez az osztály kezeli az XML formátumú mentés/betöltés.
- Metódusok:
  - save(Board, Path, String): menti a táblát és játékost XML-be.
  - load(Path): betölti a SavedGame-et XML-ból.

### Board.java
- Ez az osztály reprezentálja a játék tábláját.
- Metódusok:
  - Board(int, int): konstruktor, létrehoz egy táblát.
  - makeMove(Move): elhelyez egy lépést.
  - cellAt(int, int): visszaadja a cella tartalmát.
  - legalMove(Position): ellenőrzi, hogy legális-e a lépés.
  - winCheck(Cell): ellenőrzi a nyerést.
  - isFull(): ellenőrzi, hogy tele van-e a tábla.
  - randomAImove(): generál egy AI lépést.
  - hasNeighbor(int, int): ellenőrzi a szomszédokat.
  - print(): kiírja a táblát.

### Cell.java
- Ez egy enum a cella típusokhoz.
- Értékek: EMPTY, X, O.
- Metódusok:
  - getCharacter(): visszaadja a karaktert.

### Move.java
- Ez egy record a lépéshez.
- Mezők: position, cell.

### Player.java
- Ez egy record a játékoshoz.
- Mezők: name.

### Position.java
- Ez egy record a pozícióhoz.
- Mezők: row, col.

## Teszt fájlok leírása

### AmobaMainTest.java
- main_runs_and_exits_with_quit_input: teszteli az Amoba.main metódust, hogy lefut és kilép x inputra.

### GameIOBehaviorTest.java
- newGame_promptsForSizes_and_handlesInvalidInputs: teszteli az új játék létrehozását és érvénytelen inputokat.
- illegalMove_isReported: teszteli a hibás lépés jelentését.
- resultsCommand_printsHighscores: teszteli a results parancsot és highscore kiírást.
- whenXmlPresent_gameLoadsXmlAndPrintsPlayer: teszteli az XML betöltést és játékos kiírást.
- saveCommand_writesBothTxtAndXmlFiles: teszteli a save parancsot és fájlok írást.
- humanMove_canWin_and_removesSaveFiles: teszteli a human nyerést és fájlok törlését.

### HighscoreMockitoTest.java
- mockHighscoreTopAndVerifyAddWin: teszteli a Highscore mockolását és addWin metódust.

### HighscoreTest.java
- addWinAndTopWorks: teszteli az addWin és top metódusokat.

### TextBoardIOTest.java
- saveAndLoadRoundtripIncludesPlayerName: teszteli a save és load metódusokat.

### TextBoardIOExtraTest.java
- saveAndLoad_preservesPlayerNameAndBoard: teszteli a save és load metódusokat.

### XmlBoardIOExtraTest.java
- saveAndLoad_roundtrip_preservesDimensionsAndPlayerNameEscaped: teszteli a save és load metódusokat és escape-elést.

### BoardTest.java
- testMakeMoveAndCellAt: teszteli a makeMove és cellAt metódusokat.
- testLegalMoveBoundsAndNeighbor: teszteli a legalMove metódust és szomszédokat.
- testWinHorizontal: teszteli a vízszintes nyerési feltételt.
- testWinVertical: teszteli a függőleges nyerési feltételt.
- testWinDiagonal: teszteli az átlós nyerési feltételt.
- testRandomAIMoveIsLegal: teszteli az AI lépés legális voltát.
- testIsFull: teszteli az isFull metódust.

### CellTest.java
- testFromCharacter: teszteli a fromCharacter metódust különböző karakterekkel.