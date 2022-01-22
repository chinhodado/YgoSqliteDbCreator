package com.chin.ygowikitool.parser;

import static com.chin.ygowikitool.parser.Util.jsoupGet;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.chin.ygowikitool.entity.Card;

@RunWith(Parameterized.class)
public class YugipediaCardParserTest {
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
            YugipediaCardParser parser = new YugipediaCardParser(name, mainDom);
            card = parser.parse();
            cardCache.put(name, card);
        }
    }

//    realName, attribute, cardType, types, level, atk, def, passcode,
//    effectTypes, materials, fusionMaterials, rank, ritualSpell,
//    pendulumScale, linkMarkers, link, property, summonedBy, limitText, synchroMaterial, ritualMonster,
//    lore, archetype, ocgStatus, tcgAdvStatus, tcgTrnStatus, img;

    // may test effect types
    // may test lore for those with stable lore
    // may test ocgStatus, tcgAdvStatus, and tcgTrnStatus for those with stable status

    // skip marker, for things that changes a lot (like effectTypes, or ocgStatus) or things that are long/we don't care (lore)
    private static final String X = "--skip--";
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // normal
                { "Dark Magician", "https://yugipedia.com/wiki/Dark_Magician",
                        "", "DARK", "Monster", "Spellcaster / Normal", "7", "2500", "2100", "46986414",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "<i>The ultimate wizard in terms of attack and defense.</i>", "U", "U", "U", "Dark", "Dark Magician"},

                // effect
                { "Black Luster Soldier - Envoy of the Beginning", "https://yugipedia.com/wiki/Black_Luster_Soldier_-_Envoy_of_the_Beginning",
                        "", "LIGHT", "Monster", "Warrior / Effect", "8", "3000", "2500", "72989439",
                        X, "", "", "", "", "", "", "", "", "", "", "", "", X, X, X, X, X, X},

                // fusion
                { "Blue-Eyes Ultimate Dragon", "https://yugipedia.com/wiki/Blue-Eyes_Ultimate_Dragon",
                        "", "LIGHT", "Monster", "Dragon / Fusion", "12", "4500", "3800", "23995346",
                        "", X, X,
                        "", "", "", "", "", "", "", "", "", "", X, "U", "U", "U", X, "Blue-Eyes"},

                // ritual
                { "Zera the Mant", "https://yugipedia.com/wiki/Zera_the_Mant",
                        "", "DARK", "Monster", "Fiend / Ritual", "8", "2800", "2300", "69123138",
                        "", "", "", "", "\"Zera Ritual\"", "", "", "", "", "", "", "", "", X, "U", "U", "U", X, X},

                // synchro
                { "Genex Ally Triarm", "https://yugipedia.com/wiki/Genex_Ally_Triarm",
                        "", "DARK", "Monster", "Machine / Synchro / Effect", "6", "2400", "1600", "17760003",
                        X, X, "", "", "", "", "", "", "", "", "", X, "", X, "U", "U", "U", X, X},

                // xyz
                { "Number 39: Utopia", "https://yugipedia.com/wiki/Number_39:_Utopia",
                        "", "LIGHT", "Monster", "Warrior / Xyz / Effect", "", "2500", "2000", "84013237",
                        "Trigger Trigger", X, "", "4", "", "", "", "", "", "", "", "", "", X, "U", "U", "U", X, X},

                // pendulum
                { "Odd-Eyes Pendulum Dragon", "https://yugipedia.com/wiki/Odd-Eyes_Pendulum_Dragon",
                        "", "DARK", "Monster", "Dragon / Pendulum / Effect", "7", "2500", "2000", "16178681",
                        X, "", "", "", "", "4", "", "", "", "", "", "", "", X, "U", "U", "U", X, "Odd-Eyes"},

                // link
                { "Decode Talker", "https://yugipedia.com/wiki/Decode_Talker",
                        "", "DARK", "Monster", "Cyberse / Link / Effect", "", "2300", "", "01861629", X,
                        X, "", "", "", "", "|Bottom-Left|, |Top-Center|, |Bottom-Right|", "3", "", "", "", "", "", X, X, X, X, X, X},

                // token
                { "Emissary of Darkness Token", "https://yugipedia.com/wiki/Emissary_of_Darkness_Token",
                        "", "LIGHT", "Monster", "Fairy / Normal", "7", "?", "?", "None",
                        X, "", "", "", "", "", "", "", "", "\"Gorz the Emissary of Darkness\"", "This card cannot be in a Deck.", "", "", X, X, X, X, X, X},

                // spell
                { "Mystical Space Typhoon", "https://yugipedia.com/wiki/Mystical_Space_Typhoon",
                        "", "", "Spell", "", "", "", "", "05318639",
                        X, "", "", "", "", "", "", "", "Quick-Play", "", "", "", "", X, X, X, X, X, X},

                // trap
                { "Dark Bribe", "https://yugipedia.com/wiki/Dark_Bribe",
                        "", "", "Trap", "", "", "", "", "77538567",
                        X, "", "", "", "", "", "", "", "Counter", "", "", "", "", X, X, X, X, X, X},

                // ritual spell
                { "Zera Ritual", "https://yugipedia.com/wiki/Zera_Ritual",
                        "", "", "Spell", "", "", "", "", "81756897",
                        X, "", "", "", "", "", "", "", "Ritual", "", "", "", "\"Zera the Mant\"", X, X, X, X, X, X},

                // known forbidden
                { "Fiber Jar", "https://yugipedia.com/wiki/Fiber_Jar",
                        "", "EARTH", "Monster", "Plant / Effect", "3", "500", "500", "78706415",
                        X, "", "", "", "", "", "", "", "", "", "", "", "", X, "Forbidden", "Forbidden", "Limited", "Fiber", X},

        });
    }

    @Parameterized.Parameter(0) public String name;
    @Parameterized.Parameter(1) public String url;
    @Parameterized.Parameter(2) public String realName;
    @Parameterized.Parameter(3) public String attribute;
    @Parameterized.Parameter(4) public String cardType;
    @Parameterized.Parameter(5) public String types;
    @Parameterized.Parameter(6) public String level;
    @Parameterized.Parameter(7) public String atk;
    @Parameterized.Parameter(8) public String def;
    @Parameterized.Parameter(9) public String passcode;
    @Parameterized.Parameter(10) public String effectTypes;
    @Parameterized.Parameter(11) public String materials;
    @Parameterized.Parameter(12) public String fusionMaterials;
    @Parameterized.Parameter(13) public String rank;
    @Parameterized.Parameter(14) public String ritualSpell;
    @Parameterized.Parameter(15) public String pendulumScale;
    @Parameterized.Parameter(16) public String linkMarkers;
    @Parameterized.Parameter(17) public String link;
    @Parameterized.Parameter(18) public String property;
    @Parameterized.Parameter(19) public String summonedBy;
    @Parameterized.Parameter(20) public String limitText;
    @Parameterized.Parameter(21) public String synchroMaterial;
    @Parameterized.Parameter(22) public String ritualMonster;
    @Parameterized.Parameter(23) public String lore;
    @Parameterized.Parameter(24) public String ocgStatus;
    @Parameterized.Parameter(25) public String tcgAdvStatus;
    @Parameterized.Parameter(26) public String tcgTrnStatus;
    @Parameterized.Parameter(27) public String img;
    @Parameterized.Parameter(28) public String archetype;

    @Test
    public void parseRealName() {
        assertEquals(realName, card.getRealName());
    }

    @Test
    public void parseAttribute() {
        assertEquals(attribute, card.getAttribute());
    }

    @Test
    public void parseCardType() {
        assertEquals(cardType, card.getCardType());
    }

    @Test
    public void parseTypes() {
        assertEquals(types, card.getTypes());
    }

    @Test
    public void parseLevel() {
        assertEquals(level, card.getLevel());
    }

    @Test
    public void parseAtk() {
        assertEquals(atk, card.getAtk());
    }

    @Test
    public void parseDef() {
        assertEquals(def, card.getDef());
    }

    @Test
    public void parsePasscode() {
        assertEquals(passcode, card.getPasscode());
    }

    @Test
    public void parseEffectTypes() {
        if (effectTypes.equals(X)) return;
        assertEquals(effectTypes, card.getEffectTypes());
    }

    @Test
    public void parseMaterials() {
        if (materials.equals(X)) return;
        assertEquals(materials, card.getMaterials());
    }

    @Test
    public void parseFusionMaterials() {
        if (fusionMaterials.equals(X)) return;
        assertEquals(fusionMaterials, card.getFusionMaterials());
    }

    @Test
    public void parseRank() {
        assertEquals(rank, card.getRank());
    }

    @Test
    public void parseRitualSpell() {
        assertEquals(ritualSpell, card.getRitualSpell());
    }

    @Test
    public void parsePendulumScale() {
        assertEquals(pendulumScale, card.getPendulumScale());
    }

    @Test
    public void parseLinkMarkers() {
        assertEquals(linkMarkers, card.getLinkMarkers());
    }

    @Test
    public void parseLink() {
        assertEquals(link, card.getLink());
    }

    @Test
    public void parseProperty() {
        assertEquals(property, card.getProperty());
    }

    @Test
    public void parseSummonedBy() {
        assertEquals(summonedBy, card.getSummonedBy());
    }

    @Test
    public void parseLimitText() {
        assertEquals(limitText, card.getLimitText());
    }

    @Test
    public void parseSynchroMaterial() {
        if (synchroMaterial.equals(X)) return;
        assertEquals(synchroMaterial, card.getSynchroMaterial());
    }

    @Test
    public void parseRitualMonster() {
        assertEquals(ritualMonster, card.getRitualMonster());
    }

    @Test
    public void parseLore() {
        if (lore.equals(X)) return;
        assertEquals(lore, card.getLore());
    }

    @Test
    public void parseOcgStatus() {
        if (ocgStatus.equals(X)) return;
        assertEquals(ocgStatus, card.getOcgStatus());
    }

    @Test
    public void parseTcgAdvStatus() {
        if (tcgAdvStatus.equals(X)) return;
        assertEquals(tcgAdvStatus, card.getTcgAdvStatus());
    }

    @Test
    public void parseTcgTrnStatus() {
        if (tcgTrnStatus.equals(X)) return;
        assertEquals(tcgTrnStatus, card.getTcgTrnStatus());
    }

    @Test
    public void parseImage() {
        if (img.equals(X)) return;
        assertEquals(card.getImg().contains(img), true);
    }

    @Test
    public void parseArchetypes() {
        if (archetype.equals(X)) return;
        assertThat(card.getArchetypes(), hasItems(archetype));
    }
}
