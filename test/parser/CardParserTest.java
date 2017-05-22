package parser;

import entity.Card;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static parser.Util.jsoupGet;

/**
 * Created by Chin on 13-May-17.
 */
@RunWith(Parameterized.class)
public class CardParserTest {
    // for performance reason
    private static Map<String, Card> cardCache = new HashMap<>();
    private Card card;

    @Before
    public void setUp() throws IOException {
        if (cardCache.containsKey(name)) {
            card = cardCache.get(name);
        }
        else {
            Document mainDom = Jsoup.parse(jsoupGet(url));
            CardParser parser = new CardParser(name, mainDom);
            card = parser.parse();
            cardCache.put(name, card);
        }
    }

//    realName, attribute, cardType, types, level, atk, def, passcode,
//    effectTypes, materials, fusionMaterials, rank, ritualSpell,
//    pendulumScale, linkMarkers, link, property, summonedBy, limitText, synchroMaterial, ritualMonster,
//    lore, archetype, ocgStatus, tcgAdvStatus, tcgTrnStatus, img;

    // may test effect types
    // not testing img, archetype right now
    // may test lore for those with stable lore
    // may test ocgStatus, tcgAdvStatus, and tcgTrnStatus for those with stable status

    // skip marker, for things that changes a lot (like effectTypes, or ocgStatus) or things that are long/we don't care (lore)
    private static final String X = "--skip--";
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // normal
            { "Dark Magician", "http://yugioh.wikia.com/wiki/Dark_Magician",
                "", "DARK", "Monster", "Spellcaster", "7", "2500", "2100", "46986414", "", "", "", "", "", "", "", "", "", "", "", "", "", "<i>The ultimate wizard in terms of attack and defense.</i>", "U", "U", "U"},

            // effect
            { "Black Luster Soldier - Envoy of the Beginning", "http://yugioh.wikia.com/wiki/Black_Luster_Soldier_-_Envoy_of_the_Beginning",
                "", "LIGHT", "Monster", "Warrior / Effect", "8", "3000", "2500", "72989439", X, "", "", "", "", "", "", "", "", "", "", "", "", X, X, X, X},

            // fusion
            { "Blue-Eyes Ultimate Dragon", "http://yugioh.wikia.com/wiki/Blue-Eyes_Ultimate_Dragon",
                "", "LIGHT", "Monster", "Dragon / Fusion", "12", "4500", "3800", "23995346", "", "\"Blue-Eyes White Dragon\" + \"Blue-Eyes White Dragon\" + \"Blue-Eyes White Dragon\"", "\"Blue-Eyes White Dragon\"", "", "", "", "", "", "", "", "", "", "", X, "U", "U", "U"},

            // ritual
            { "Zera the Mant", "http://yugioh.wikia.com/wiki/Zera_the_Mant",
                "", "DARK", "Monster", "Fiend / Ritual", "8", "2800", "2300", "69123138", "", "", "", "", "\"Zera Ritual\"", "", "", "", "", "", "", "", "", X, "U", "U", "U"},

            // synchro
            { "Genex Ally Triarm", "http://yugioh.wikia.com/wiki/Genex_Ally_Triarm",
                "", "DARK", "Monster", "Machine / Synchro / Effect", "6", "2400", "1600", "17760003", X, "\"Genex Controller\" + 1 or more non-Tuner monsters", "", "", "", "", "", "", "", "", "", "\"Genex Controller\"", "", X, "U", "U", "U"},

            // xyz
            { "Number 39: Utopia", "http://yugioh.wikia.com/wiki/Number_39:_Utopia",
                "", "LIGHT", "Monster", "Warrior / Xyz / Effect", "", "2500", "2000", "84013237", "Trigger Trigger", "2 Level 4 monsters", "", "4", "", "", "", "", "", "", "", "", "", X, "U", "U", "U"},

            // pendulum
            { "Odd-Eyes Pendulum Dragon", "http://yugioh.wikia.com/wiki/Odd-Eyes_Pendulum_Dragon",
                "", "DARK", "Monster", "Dragon / Pendulum / Effect", "7", "2500", "2000", "16178681", X, "", "", "", "", "4", "", "", "", "", "", "", "", X, "U", "U", "U"},

            // link
            { "Decode Talker", "http://yugioh.wikia.com/wiki/Decode_Talker",
                "", "DARK", "Monster", "Cyberse / Link / Effect", "", "2300", "", "01861629", X, "2+ Effect Monsters", "", "", "", "", "Top, Bottom-Left, Bottom-Right", "3", "", "", "", "", "", X, X, X, X},

            // token
            { "Emissary of Darkness Token", "http://yugioh.wikia.com/wiki/Emissary_of_Darkness_Token",
                "", "LIGHT", "Monster", "Fairy / Token", "7", "?", "?", "", X, "", "", "", "", "", "", "", "", "\"Gorz the Emissary of Darkness\"", "This card cannot be in a Deck.", "", "", X, X, X, X},

            // spell
            { "Mystical Space Typhoon", "http://yugioh.wikia.com/wiki/Mystical_Space_Typhoon",
                "", "", "Spell", "", "", "", "", "05318639", X, "", "", "", "", "", "", "", "Quick-Play", "", "", "", "", X, X, X, X},

            // trap
            { "Dark Bribe", "http://yugioh.wikia.com/wiki/Dark_Bribe",
                    "", "", "Trap", "", "", "", "", "77538567", X, "", "", "", "", "", "", "", "Counter", "", "", "", "", X, X, X, X},

            // ritual spell
            { "Zera Ritual", "http://yugioh.wikia.com/wiki/Zera_Ritual",
                "", "", "Spell", "", "", "", "", "81756897", X, "", "", "", "", "", "", "", "Ritual", "", "", "", "\"Zera the Mant\"", X, X, X, X},

            // known forbidden
            { "Fiber Jar", "http://yugioh.wikia.com/wiki/Fiber_Jar",
                "", "EARTH", "Monster", "Plant / Effect", "3", "500", "500", "78706415", X, "", "", "", "", "", "", "", "", "", "", "", "", X, "Forbidden", "Forbidden", "Limited"},

        });
    }

    @Parameter(0) public String name;
    @Parameter(1) public String url;
    @Parameter(2) public String realName;
    @Parameter(3) public String attribute;
    @Parameter(4) public String cardType;
    @Parameter(5) public String types;
    @Parameter(6) public String level;
    @Parameter(7) public String atk;
    @Parameter(8) public String def;
    @Parameter(9) public String passcode;
    @Parameter(10) public String effectTypes;
    @Parameter(11) public String materials;
    @Parameter(12) public String fusionMaterials;
    @Parameter(13) public String rank;
    @Parameter(14) public String ritualSpell;
    @Parameter(15) public String pendulumScale;
    @Parameter(16) public String linkMarkers;
    @Parameter(17) public String link;
    @Parameter(18) public String property;
    @Parameter(19) public String summonedBy;
    @Parameter(20) public String limitText;
    @Parameter(21) public String synchroMaterial;
    @Parameter(22) public String ritualMonster;
    @Parameter(23) public String lore;
    @Parameter(24) public String ocgStatus;
    @Parameter(25) public String tcgAdvStatus;
    @Parameter(26) public String tcgTrnStatus;

    @Test
    public void parseRealName() {
        assertEquals(card.getRealName(), realName);
    }

    @Test
    public void parseAttribute() {
        assertEquals(card.getAttribute(), attribute);
    }

    @Test
    public void parseCardType() {
        assertEquals(card.getCardType(), cardType);
    }

    @Test
    public void parseTypes() {
        assertEquals(card.getTypes(), types);
    }

    @Test
    public void parseLevel() {
        assertEquals(card.getLevel(), level);
    }

    @Test
    public void parseAtk() {
        assertEquals(card.getAtk(), atk);
    }

    @Test
    public void parseDef() {
        assertEquals(card.getDef(), def);
    }

    @Test
    public void parsePasscode() {
        assertEquals(card.getPasscode(), passcode);
    }

    @Test
    public void parseEffectTypes() {
        if (effectTypes.equals(X)) return;
        assertEquals(card.getEffectTypes(), effectTypes);
    }

    @Test
    public void parseMaterials() {
        assertEquals(card.getMaterials(), materials);
    }

    @Test
    public void parseFusionMaterials() {
        assertEquals(card.getFusionMaterials(), fusionMaterials);
    }

    @Test
    public void parseRank() {
        assertEquals(card.getRank(), rank);
    }

    @Test
    public void parseRitualSpell() {
        assertEquals(card.getRitualSpell(), ritualSpell);
    }

    @Test
    public void parsePendulumScale() {
        assertEquals(card.getPendulumScale(), pendulumScale);
    }

    @Test
    public void parseLinkMarkers() {
        assertEquals(card.getLinkMarkers(), linkMarkers);
    }

    @Test
    public void parseLink() {
        assertEquals(card.getLink(), link);
    }

    @Test
    public void parseProperty() {
        assertEquals(card.getProperty(), property);
    }

    @Test
    public void parseSummonedBy() {
        assertEquals(card.getSummonedBy(), summonedBy);
    }

    @Test
    public void parseLimitText() {
        assertEquals(card.getLimitText(), limitText);
    }

    @Test
    public void parseSynchroMaterial() {
        assertEquals(card.getSynchroMaterial(), synchroMaterial);
    }

    @Test
    public void parseRitualMonster() {
        assertEquals(card.getRitualMonster(), ritualMonster);
    }

    @Test
    public void parseLore() {
        if (lore.equals(X)) return;
        assertEquals(card.getLore(), lore);
    }

    @Test
    public void parseOcgStatus() {
        if (ocgStatus.equals(X)) return;
        assertEquals(card.getOcgStatus(), ocgStatus);
    }

    @Test
    public void parseTcgAdvStatus() {
        if (tcgAdvStatus.equals(X)) return;
        assertEquals(card.getTcgAdvStatus(), tcgAdvStatus);
    }

    @Test
    public void parseTcgTrnStatus() {
        if (tcgTrnStatus.equals(X)) return;
        assertEquals(card.getTcgTrnStatus(), tcgTrnStatus);
    }
}