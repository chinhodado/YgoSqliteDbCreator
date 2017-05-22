package parser;

import entity.Booster;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static parser.Util.jsoupGet;

/**
 * Created by Chin on 22-May-17.
 */
@RunWith(Parameterized.class)
public class BoosterParserTest {
    // for performance reason
    private static Map<String, Booster> boosterCache = new HashMap<>();
    private Booster booster;

    @Parameterized.Parameter(0) public String name;
    @Parameterized.Parameter(1) public String url;
    @Parameterized.Parameter(2) public String enReleaseDate;
    @Parameterized.Parameter(3) public String jpReleaseDate;
    @Parameterized.Parameter(4) public String skReleaseDate;
    @Parameterized.Parameter(5) public String worldwideReleaseDate;

    @Before
    public void setUp() throws IOException {
        if (boosterCache.containsKey(name)) {
            booster = boosterCache.get(name);
        }
        else {
            Document mainDom = Jsoup.parse(jsoupGet(url));
            BoosterParser parser = new BoosterParser(name, mainDom);
            booster = parser.parse();
            boosterCache.put(name, booster);
        }
    }

    private static final String X = "--skip--";
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "Hidden Arsenal", "http://yugioh.wikia.com/wiki/Hidden_Arsenal", "November 10, 2009", null, "May 25, 2010", null },
            { "Premium Pack 10", "http://yugioh.wikia.com/wiki/Premium_Pack_10", null, "March 8, 2007", null, null },
            { "Invasion of Chaos", "http://yugioh.wikia.com/wiki/Invasion_of_Chaos", "March 1, 2004", null, "June 28, 2005", "October 18, 2005" },
        });
    }

    @Test
    public void parseEnReleaseDate() {
        assertEquals(booster.getEnReleaseDate(), enReleaseDate);
    }

    @Test
    public void parseJpReleaseDate() {
        assertEquals(booster.getJpReleaseDate(), jpReleaseDate);
    }

    @Test
    public void parseSkReleaseDate() {
        assertEquals(booster.getSkReleaseDate(), skReleaseDate);
    }

    @Test
    public void parseWorldwideReleaseDate() {
        assertEquals(booster.getWorldwideReleaseDate(), worldwideReleaseDate);
    }
}
