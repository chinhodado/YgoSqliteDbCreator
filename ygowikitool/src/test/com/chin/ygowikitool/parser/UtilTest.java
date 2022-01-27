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

    @Test
    public void whenGetFullYugipediaImageLink_ThenReturnCorrectValue() {
        String shortened = "5aGranmargtheRockMonarch-SBCB-EN-C-1E.png";
        String original = Util.getFullYugipediaImageLink(shortened);
        assertThat(original, is("https://ms.yugipedia.com//5/5a/GranmargtheRockMonarch-SBCB-EN-C-1E.png"));

        shortened = "deMountainWarrior-SB-JP-C.jpg";
        original = Util.getFullYugipediaImageLink(shortened);
        assertThat(original, is("https://ms.yugipedia.com//d/de/MountainWarrior-SB-JP-C.jpg"));
    }

    @Test
    public void whenGetScaledYugipediaImageLink_ThenReturnCorrectValue() {
        String original = "https://ms.yugipedia.com//thumb/5/5a/GranmargtheRockMonarch-SBCB-EN-C-1E.png/300px-GranmargtheRockMonarch-SBCB-EN-C-1E.png";
        String scaled = Util.getScaledYugipediaImageLink(original, 500);
        assertThat(scaled, is("https://ms.yugipedia.com//thumb/5/5a/GranmargtheRockMonarch-SBCB-EN-C-1E.png/500px-GranmargtheRockMonarch-SBCB-EN-C-1E.png"));

        original = "https://ms.yugipedia.com//d/de/MountainWarrior-SB-JP-C.jpg";
        scaled = Util.getScaledYugipediaImageLink(original, 500);
        assertThat(scaled, is("https://ms.yugipedia.com//thumb/d/de/MountainWarrior-SB-JP-C.jpg/500px-MountainWarrior-SB-JP-C.jpg"));
    }
}
