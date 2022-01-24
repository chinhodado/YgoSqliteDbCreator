package com.chin.ygowikitool.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class UtilTest {
    @Test
    public void whenGetShortenedYugipediaImageLink_ThenReturnCorrectValue() {
        String original = "https://ms.yugipedia.com//thumb/5/5a/GranmargtheRockMonarch-SBCB-EN-C-1E.png/300px-GranmargtheRockMonarch-SBCB-EN-C-1E.png";
        String shortened = Util.getShortenedYugipediaImageLink(original);
        assertThat(shortened, is("5aGranmargtheRockMonarch-SBCB-EN-C-1E.png"));

        original = "https://ms.yugipedia.com//d/de/MountainWarrior-SB-JP-C.jpg";
        shortened = Util.getShortenedYugipediaImageLink(original);
        assertThat(shortened, is("deMountainWarrior-SB-JP-C.jpg"));
    }
}
