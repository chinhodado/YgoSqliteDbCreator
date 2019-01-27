package com.chin.ygowikitool.api;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class YugiohWikiaApiTest {
    private YugiohWikiaApi api;

    @Before
    public void setup() {
        api = new YugiohWikiaApi();
    }

    @Test
    public void whenGetTcgCardMapThenItContainsSangan() throws IOException, JSONException {
        assertTrue(api.getCardMap(true).containsKey("Sangan"));
    }

    @Test
    public void whenGetTcgCardMapThenItDoesNotContain30000YearWhiteTurtle() throws IOException, JSONException {
        assertTrue(!api.getCardMap(true).containsKey("30,000-Year White Turtle"));
    }

    @Test
    public void whenGetOcgCardMapThenItContains30000YearWhiteTurtle() throws IOException, JSONException {
        assertTrue(api.getCardMap(false).containsKey("30,000-Year White Turtle"));
    }

    @Test
    public void whenGetTcgBoosterMapThenItContainsSoulFusion() throws IOException, JSONException {
        assertTrue(api.getBoosterMap(true).containsKey("Soul Fusion"));
    }

    @Test
    public void whenGetOcgBoosterMapThenItContainsAdventOfUnion() throws IOException, JSONException {
        assertTrue(api.getBoosterMap(false).containsKey("Advent of Union"));
    }

    @Test
    public void whenGetTcgBoosterMapThenItDoesNotContainAdventOfUnion() throws IOException, JSONException {
        assertTrue(!api.getBoosterMap(true).containsKey("Advent of Union"));
    }
}
