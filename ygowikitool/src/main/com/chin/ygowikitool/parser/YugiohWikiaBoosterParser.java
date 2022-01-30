package com.chin.ygowikitool.parser;

import com.chin.ygowikitool.entity.Booster;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parsing booster dom
 * <p>
 * Created by Chin on 06-Feb-17.
 */
public class YugiohWikiaBoosterParser implements BoosterParser {
    private final Element dom;
    private final String boosterName;

    public YugiohWikiaBoosterParser(String boosterName, Document dom) {
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
        booster.setImgSrc(YugiohWikiUtil.getShortenedWikiaImageLink(getImageLink()));
        return booster;
    }

    private String getJapaneseReleaseDate() {
        return getReleaseDate("Japan");
    }

    private String getEnglishReleaseDate() {
        return getReleaseDate("North America");
    }

    private String getSouthKoreaReleaseDate() {
        return getReleaseDate("South Korea");
    }

    private String getWorldwideReleaseDate() {
        return getReleaseDate("Worldwide");
    }

    private String getReleaseDate(String type) {
        try {
            Elements sections = dom.getElementsByClass("portable-infobox").first().select("section.pi-item");
            for (Element section : sections) {
                if (section.text().startsWith("Release dates")) { // heuristic
                    String date = null;
                    Elements rows = section.select("div.pi-item");
                    for (Element row : rows) {
                        Elements headers = row.select("h3.pi-data-label");
                        if (headers.size() > 0) {
                            Element header = headers.get(0);

                            if (header.text().startsWith(type)) {
                                date = row.select("div.pi-data-value").first().text();
                            }

                            if (header.text().equals(type)){
                                return date;
                            }
                        }
                    }

                    return date;
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
            return dom.getElementsByClass("image-thumbnail").first().attr("href");
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getIntroText() {
        try {
            return dom.select("#mw-content-text > p").first().text();
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getFeatureText() {
        try {
            return dom.select("#mw-content-text > ul").first().text();
        }
        catch (Exception e) {
            return null;
        }
    }
}
