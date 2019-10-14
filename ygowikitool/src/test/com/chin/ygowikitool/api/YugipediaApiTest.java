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
    private static Map<String, String> rulings;

    @BeforeClass
    public static void setupAll() throws IOException, JSONException {
        YugipediaApi api = new YugipediaApi();
        rulings = api.getRulingMap();
    }

    @Test
    public void whenGetRulingListThenItContainsSangan() {
        assertTrue(rulings.containsKey("Sangan"));
    }

    @Test
    public void whenGetThunderDragonTitanRulingByPageId_ThenReturnCorrectText() {
        String expected = "<div> \n" +
                " <h2>OCG Rulings</h2> \n" +
                " <ul>\n" +
                "  <li>This monster can be Special Summoned by 2 methods; by using a card such as \"Polymerization\" to fuse 3 \"Thunder Dragon\" monsters, or by its own procedure by banishing 1 Thunder-Type monster from your hand and 1 Thunder-Type Fusion Monster from your Monster Zone, other than \"Thunder Dragon Titan\", during your Main Phase. (Both methods are treated as \"properly\" Special Summoning this monster.)</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>When Special Summoning this card with \"Polymerization\", etc., it is Special Summoned by that card's activated effect, but when you Special Summon it by banishing a monster from your hand and in your Monster Zone, it does not start a Chain.</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>The effect of this card that destroys 1 card on the field is a Quick Effect. (It does not target. This effect is activated by chaining directly to the effect of a Thunder-Type monster activated from your or your opponent's hand.)</li>\n" +
                " </ul> \n" +
                " <ul>\n" +
                "  <li>The effect of this card that allows you to banish 2 Thunder-Type monsters from your Graveyard instead when it would be destroyed by battle or card effect is a Continuous Effect. (This effect can be applied during the Damage Step.)</li>\n" +
                " </ul>     \n" +
                "</div>";

        YugipediaApi api = new YugipediaApi();
        assertEquals(expected, api.getCardRulingByPageId(rulings.get("Thunder Dragon Titan")));
    }

    @Test
    public void whenGetSuccessProbability0RulingByCardName_ThenReturnCorrectText() {
        YugipediaApi api = new YugipediaApi();
        String ruling = api.getCardRulingByPageId(rulings.get("Success Probability 0%"));
        assertThat(ruling, containsString("Destiny End Dragoon"));
    }

    @Test
    public void whenGetArchfiendOathRulingByCardName_ThenReturnCorrectText() {
        YugipediaApi api = new YugipediaApi();
        String ruling = api.getCardRulingByPageId(rulings.get("Archfiend's Oath"));
        assertThat(ruling, containsString("Spell Economics"));
    }

    @Test
    public void whenGetLevelDownRulingByCardName_ThenReturnCorrectText() {
        YugipediaApi api = new YugipediaApi();
        String ruling = api.getCardRulingByPageId(rulings.get("Level Down!?"));
        assertThat(ruling, containsString("Armed Dragon"));
    }
}
