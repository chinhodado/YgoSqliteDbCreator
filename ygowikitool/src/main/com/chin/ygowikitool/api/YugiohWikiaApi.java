package com.chin.ygowikitool.api;

import com.chin.ygowikitool.entity.Booster;
import com.chin.ygowikitool.entity.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.chin.ygowikitool.parser.BoosterParser;
import com.chin.ygowikitool.parser.YugiohWikiaCardParser;
import com.chin.ygowikitool.parser.YugiohWikiUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.chin.ygowikitool.parser.YugiohWikiUtil.jsoupGet;

public class YugiohWikiaApi implements YugiohApi {
    @Override
    public void initialize() {

    }

    @Override
    public Map<String, String> getCardMap(boolean isTcg) throws IOException, JSONException {
        return getCardMap(null, new HashMap<>(8192), isTcg);
    }

    private Map<String, String> getCardMap(String offset, Map<String, String> cardMap, boolean isTcg) throws JSONException, IOException {
        // this will return up to 5000 articles in the TCG_cards/OCG_cards category.
        // Note that this is not always up-to-date, as newly added articles may take
        // a day or two before showing up in here
        String url;
        if (isTcg) {
            url = "https://yugioh.wikia.com/api/v1/Articles/List?category=TCG_cards&limit=5000&namespaces=0";
        }
        else {
            url = "https://yugioh.wikia.com/api/v1/Articles/List?category=OCG_cards&limit=5000&namespaces=0";
        }

        if (offset != null) {
            url = url + "&offset=" + offset;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONArray("items");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject cardInfo = myArray.getJSONObject(i);
            String cardName = cardInfo.getString("title");
            if (cardName.trim().endsWith("(temp)")) {
                continue;
            }

            if (!cardMap.containsKey(cardName)) {
                String cardUrl = cardInfo.getString("url");
                // myArray.getJSONObject(i).getString("id")};
                cardMap.put(cardName, cardUrl);
            }
        }

        if (myJSON.has("offset")) {
            return getCardMap((String) myJSON.get("offset"), cardMap, isTcg);
        }
        else {
            return cardMap;
        }
    }

    @Override
    public Map<String, String> getBoosterMap(boolean isTcg) throws IOException, JSONException {
        return getBoosterMap(null, new HashMap<>(), isTcg);
    }

    private Map<String, String> getBoosterMap(String offset, Map<String, String> boosterMap, boolean isTcg) throws JSONException, IOException {
        String url;
        if (isTcg) {
            url = "https://yugioh.wikia.com/api/v1/Articles/List?category=TCG_Booster_Packs&limit=5000&namespaces=0";
        }
        else {
            url = "https://yugioh.wikia.com/api/v1/Articles/List?category=OCG_Booster_Packs&limit=5000&namespaces=0";
        }

        if (offset != null) {
            url = url + "&offset=" + offset;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONArray("items");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject boosterInfo = myArray.getJSONObject(i);
            String boosterName = boosterInfo.getString("title");
            if (boosterName.trim().endsWith("(temp)")) {
                continue;
            }
            if (!boosterMap.containsKey(boosterName)) {
                String boosterUrl = boosterInfo.getString("url");
                // myArray.getJSONObject(i).getString("id")};
                boosterMap.put(boosterName, boosterUrl);
            }
        }

        if (myJSON.has("offset")) {
            return getBoosterMap((String) myJSON.get("offset"), boosterMap, isTcg);
        }
        else {
            return boosterMap;
        }
    }

    @Override
    public Booster getBooster(String boosterName, String boosterLink) throws IOException {
        String boosterUrl = "https://yugioh.wikia.com" + boosterLink;
        Document mainDom = Jsoup.parse(jsoupGet(boosterUrl));
        BoosterParser parser = new BoosterParser(boosterName, mainDom);
        Booster booster = parser.parse();

        return booster;
    }

    @Override
    public Card getCard(String cardName, String cardLink) throws IOException {
        String cardUrl = "https://yugioh.wikia.com" + cardLink;
        Document mainDom = Jsoup.parse(jsoupGet(cardUrl));
        YugiohWikiaCardParser parser = new YugiohWikiaCardParser(cardName, mainDom);
        Card card = parser.parse();

        return card;
    }

    @Override
    public String getRuling(String cardLink) {
        String ruling = "";
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugioh.wikia.com/wiki/Card_Rulings:" + cardLink.substring(6)));
            ruling = getCardInfoGeneric(dom, false);
        }
        catch (Exception e) { /* do nothing */ }
        return ruling;
    }

    @Override
    public String getTips(String cardLink) {
        String tips = "";
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugioh.wikia.com/wiki/Card_Tips:" + cardLink.substring(6)));
            tips = getCardInfoGeneric(dom, true);
        }
        catch (Exception e) { /* do nothing */ }
        return tips;
    }

    @Override
    public String getTrivia(String cardLink) {
        String trivia = "";
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugioh.wikia.com/wiki/Card_Trivia:" + cardLink.substring(6)));
            trivia = getCardInfoGeneric(dom, false);
        }
        catch (Exception e) { /* do nothing */ }
        return trivia;
    }

    private String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return YugiohWikiUtil.getCleanedHtml(content, isTipsPage, false);
    }
}
