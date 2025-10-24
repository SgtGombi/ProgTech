package hu.gombi.amoba.db;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HighscoreMockitoTest {

    @Test
    public void mockHighscoreTopAndVerifyAddWin() throws Exception {
        Highscore hs = mock(Highscore.class);

        Map<String,Integer> fake = new LinkedHashMap<>();
        fake.put("Alice", 3);
        when(hs.top()).thenReturn(fake);

        // call the stubbed method
        Map<String,Integer> result = hs.top();
        assertEquals(1, result.size());
        assertEquals(3, result.get("Alice"));

        // verify that addWin can be invoked and is recorded by Mockito
        hs.addWin("Bob");
        verify(hs).addWin("Bob");
    }
}
