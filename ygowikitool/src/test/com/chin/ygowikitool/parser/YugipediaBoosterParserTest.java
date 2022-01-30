package com.chin.ygowikitool.parser;

import static com.chin.ygowikitool.parser.YugiohWikiUtil.jsoupGet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.chin.ygowikitool.entity.Booster;

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

@RunWith(Parameterized.class)
public class YugipediaBoosterParserTest {
    // for performance reason
    private static Map<String, Booster> boosterCache = new HashMap<>();
    private Booster booster;

    @Parameterized.Parameter(0) public String name;
    @Parameterized.Parameter(1) public String url;
    @Parameterized.Parameter(2) public String enReleaseDate;
    @Parameterized.Parameter(3) public String jpReleaseDate;
    @Parameterized.Parameter(4) public String skReleaseDate;
    @Parameterized.Parameter(5) public String worldwideReleaseDate;
    @Parameterized.Parameter(6) public String img;

    @Before
    public void setUp() throws IOException {
        if (boosterCache.containsKey(name)) {
            booster = boosterCache.get(name);
        }
        else {
            Document mainDom = Jsoup.parse(jsoupGet(url));
            YugipediaBoosterParser parser = new YugipediaBoosterParser(name, mainDom);
            booster = parser.parse();
            boosterCache.put(name, booster);
        }
    }

    private static final String X = "--skip--";
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Hidden Arsenal", "https://yugipedia.com/wiki/Hidden_Arsenal", "November 10, 2009", null, "May 25, 2010", null, "HA01" },
                { "Premium Pack 10", "https://yugipedia.com/wiki/Premium_Pack_10", null, "March 8, 2007", null, null, "PP10" },
                { "Invasion of Chaos", "https://yugipedia.com/wiki/Invasion_of_Chaos", "March 1, 2004", null, "June 28, 2005", "June 25, 2007", "IOC" },
        });
    }

    @Test
    public void parseEnReleaseDate() {
        assertThat(booster.getEnReleaseDate(), is(enReleaseDate));
    }

    @Test
    public void parseJpReleaseDate() {
        assertThat(booster.getJpReleaseDate(), is(jpReleaseDate));
    }

    @Test
    public void parseSkReleaseDate() {
        assertThat(booster.getSkReleaseDate(), is(skReleaseDate));
    }

    @Test
    public void parseWorldwideReleaseDate() {
        assertThat(booster.getWorldwideReleaseDate(), is(worldwideReleaseDate));
    }

    @Test
    public void parseImage() {
        assertTrue(booster.getImgSrc().contains(img));
    }
}
