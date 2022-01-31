package com.chin.ygowikitool.parser;

import static com.chin.ygowikitool.parser.YugiohWikiUtil.logLine;

import com.chin.ygowikitool.entity.Booster;
import com.chin.ygowikitool.entity.Card;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

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
        String imageLink = getImageLink();
        booster.setFullImgSrc(imageLink);
        booster.setShortenedImgSrc(YugiohWikiUtil.getShortenedYugipediaImageLink(imageLink));

        booster.setIntroText(getIntroText());
        booster.setFeatureText(getFeatureText());
        booster.setCardMap(getCardMap());

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

    private String getIntroText() {
        try {
            return dom.select(".mw-parser-output > p").first().text();
        }
        catch (Exception e) {
            return null;
        }
    }

    private String getFeatureText() {
        try {
            return dom.select(".mw-parser-output > ul").first().text();
        }
        catch (Exception e) {
            return null;
        }
    }

    private Map<String, Card> getCardMap() {
        Map<String, Card> cards = new HashMap<>();
        try {
            Elements rows = dom.getElementsByClass("wikitable").first()
                    .getElementsByTag("tbody").first()
                    .getElementsByTag("tr");
            for (Element row : rows) {
                try {
                    Elements cells = row.getElementsByTag("td");
                    String setNumber = cells.get(0).text();
                    String cardName = cells.get(1).text();

                    if (cardName != null && cardName.length() > 2 && cardName.startsWith("\"") && cardName.endsWith("\"")) {
                        cardName = cardName.substring(1, cardName.length() - 1);
                    }

                    String rarity = "", category = "";
                    if (cells.size() == 4) {
                        // table without Japanese name column
                        rarity = cells.get(2).text();
                        category = cells.get(3).text();
                    }
                    else if (cells.size() == 5) {
                        // table with Japanese name column
                        // String jpName = cells.get(2).text();
                        rarity = cells.get(3).text();
                        category = cells.get(4).text();
                    }

                    Card card = new Card();
                    card.setName(cardName);
                    card.setSetNumber(setNumber);
                    card.setRarity(rarity);
                    card.setCategory(category);
                    cards.put(cardName, card);
                }
                catch (Exception e) {
                    // do nothing
                }
            }
        }
        catch (Exception e) {
            logLine("Unable to get card list for booster: " + boosterName);
        }

        return cards;
    }
}
