package com.chin.ygowikitool.api;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class YugipediaApiTest {
    private static YugipediaApi api;
    @BeforeClass
    public static void setupAll() throws IOException, JSONException {
        api = new YugipediaApi();
        api.initialize();
    }

//    @Test
//    public void whenGetRulingListThenItContainsSangan() {
//        assertTrue(rulings.containsKey("Sangan"));
//    }
//
//    @Test
//    public void whenGetTriviaListThenItContainsFiberJar() {
//        assertTrue(trivia.containsKey("Fiber Jar"));
//    }

    @Test
    public void whenGetThunderDragonTitanRulingByPageId_ThenReturnCorrectText() {
        String expected = "<div> \n" +
                " <h2>OCG Rulings</h2> \n" +
                " <ul>\n" +
                "  <li>This monster can be Special Summoned by 2 methods; by using a card such as \"Polymerization\" to fuse 3 \"Thunder Dragon\" monsters, or by its own procedure by banishing 1 Thunder monster from your hand and 1 Thunder Fusion Monster from your Monster Zone, other than \"Thunder Dragon Titan\", during your Main Phase. (Both methods are treated as \"properly\" Special Summoning this monster.)</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>When Special Summoning this card with \"Polymerization\", etc., it is Special Summoned by that card's activated effect, but when you Special Summon it by banishing a monster from your hand and in your Monster Zone, it does not start a Chain.</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>The effect of this card that destroys 1 card on the field is a Quick Effect. (It does not target. This effect is activated by chaining directly to the effect of a Thunder monster activated from your or your opponent's hand.)</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>The effect of this card that allows you to banish 2 Thunder-Type monsters from your GY instead when it would be destroyed by card effect is a Continuous Effect. (This effect can be applied during the Damage Step.)</li>\n" +
                " </ul>     \n" +
                "</div>";

        assertEquals(expected, api.getRuling("Thunder Dragon Titan"));
    }

    @Test
    public void whenGetSuccessProbability0Ruling_ThenReturnCorrectText() {
        String ruling = api.getRuling("Success Probability 0%");
        assertThat(ruling, containsString("Destiny End Dragoon"));
    }

    @Test
    public void whenGetArchfiendOathRuling_ThenReturnCorrectText() {
        String ruling = api.getRuling("Archfiend's Oath");
        assertThat(ruling, containsString("Spell Economics"));
    }

    @Test
    public void whenGetLevelDownRuling_ThenReturnCorrectText() {
        String ruling = api.getRuling("Level Down!?");
        assertThat(ruling, containsString("Armed Dragon"));
    }

    @Test
    public void whenGetFiberJarTrivia_ThenReturnCorrectText() {
        String ruling = api.getTrivia("Fiber Jar");
        assertThat(ruling, containsString("Castle in the Sky"));
    }
}
