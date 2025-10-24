package hu.gombi.amoba;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

// --- Ez az osztaly teszteli az AmobaMain osztalyt
public class AmobaMainTest {
    private final PrintStream originalOut = System.out;
    private final java.io.InputStream originalIn = System.in;

    // --- Minden teszt utan takaritja a fajlokat
    @AfterEach
    void cleanup() throws Exception {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Files.deleteIfExists(Path.of("board.xml"));
        Files.deleteIfExists(Path.of("board.txt"));
    }

    // --- Ez a teszt ellenorzi, hogy a main metodus lefut es kilep
    @Test
    void main_runs_and_exits_with_quit_input() throws Exception {
        // --- Bemeneteket ad a prompt-okhoz: sorok, oszlopok, jatekos nev, kilepes
        System.setIn(new ByteArrayInputStream("4\n4\nPlayerMain\nx\n".getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        Amoba.main(new String[]{});

        String out = bos.toString();
        assertNotNull(out);
    }
}
