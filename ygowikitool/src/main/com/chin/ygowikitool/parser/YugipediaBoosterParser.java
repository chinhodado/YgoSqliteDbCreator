package com.chin.ygowikitool.parser;

import com.chin.ygowikitool.entity.Booster;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YugipediaBoosterParser implements BoosterParser {
    private final Element dom;
    private final String boosterName;

    public YugipediaBoosterParser(String boosterName, Document dom) {
        this.boosterName = boosterName;
        Element elem = dom.getElementById("mw-content-text");
        YugiohWikiUtil.removeSupTag(elem);
        this.dom = elem;
    }

    @Override
    public Booster parse() {
        Booster booster = new Booster();
        booster.setBoosterName(boosterName);
        booster.setJpReleaseDate(getJapaneseReleaseDate());
        booster.setEnReleaseDate(getEnglishReleaseDate());
        booster.setSkReleaseDate(getSouthKoreaReleaseDate());
        booster.setWorldwideReleaseDate(getWorldwideReleaseDate());
        booster.setImgSrc(YugiohWikiUtil.getShortenedYugipediaImageLink(getImageLink()));
        return booster;
    }

    private String getJapaneseReleaseDate() {
        return getReleaseDate("Japanese");
    }

    private String getEnglishReleaseDate() {
        return getReleaseDate("English (na)");
    }

    private String getSouthKoreaReleaseDate() {
        return getReleaseDate("Korean");
    }

    private String getWorldwideReleaseDate() {
        return getReleaseDate("English (world)");
    }

    private String getReleaseDate(String type) {
        try {
            Elements rows = dom.getElementsByClass("infobox").first().getElementsByTag("tr");
            boolean releaseDateFound = false;
            for (Element row : rows) {
                // replace nbsp
                String rowText = row.text().replace("\u00a0", " ");
                if (!releaseDateFound) {
                    if (rowText.startsWith("Release dates")) {
                        releaseDateFound = true;
                        //continue;
                    }
                }
                else {
                    if (rowText.startsWith(type)) {
                        String date = row.getElementsByTag("td").first().text();
                        return date;
                    }
                }
            }
        }
        catch (Exception e) {
//            e.printStackTrace();
        }

        return null;
    }

    private String getImageLink() {
        try {
            Elements imageLinks = dom.getElementsByClass("image");
            for (Element imgLink : imageLinks) {
                String title = imgLink.attr("title");
                if (title != null && !"".equals(title)) {
                    // The main image link should be the only one with title
                    String link = imgLink.getElementsByTag("img").first().attr("src");
                    return link;
                }
            }

            return null;
        }
        catch (Exception e) {
            return null;
        }
    }
}
