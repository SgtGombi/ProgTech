package hu.gombi.amoba.db;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// --- Ez az osztaly teszteli a Highscore osztalyt Mockito mock objektumokkal
public class HighscoreMockitoTest {

    // --- Ez a teszt metodus mockolja a Highscore-t es ellenorzi az addWin metodust
    @Test
    public void mockHighscoreTopAndVerifyAddWin() throws Exception {
        Highscore hs = mock(Highscore.class);

        Map<String, Integer> fake = new LinkedHashMap<>();
        fake.put("Alice", 3);
        when(hs.top()).thenReturn(fake);

        // --- Meghivja a mockolt top() metodust
        Map<String, Integer> result = hs.top();
        assertEquals(1, result.size());
        assertEquals(3, result.get("Alice"));

        // --- Meghivja es ellenorzi az addWin metodust
        hs.addWin("Bob");
        verify(hs).addWin("Bob");
    }
}
