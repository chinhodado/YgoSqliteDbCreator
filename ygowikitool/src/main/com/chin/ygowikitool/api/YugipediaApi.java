package com.chin.ygowikitool.api;

import static com.chin.ygowikitool.parser.YugiohWikiUtil.jsoupGet;
import static com.chin.ygowikitool.parser.YugiohWikiUtil.logLine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.chin.ygowikitool.entity.Booster;
import com.chin.ygowikitool.entity.Card;
import com.chin.ygowikitool.parser.YugiohWikiUtil;
import com.chin.ygowikitool.parser.YugipediaCardParser;

public class YugipediaApi implements YugiohApi {
    private Map<String, String> yugipediaRulingMap;
    private Map<String, String> yugipediaTipMap;
    private Map<String, String> yugipediaTriviaMap;

    @Override
    public void initialize() throws IOException {
        logLine("Fetching ruling list from Yugipedia");
        yugipediaRulingMap = getRulingMap();
        logLine("Fetching tip list from Yugipedia");
        yugipediaTipMap = getTipMap();
        logLine("Fetching trivia list from Yugipedia");
        yugipediaTriviaMap = getTriviaMap();
    }

    public String getCardRulingByCardName(String cardName) {
        String encodedCardName = getEncodedCardName(cardName);
        String url = "https://yugipedia.com/wiki/Card_Rulings:" + encodedCardName;

        try {
            Document dom = Jsoup.parse(jsoupGet(url));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getCardTipsByCardName(String cardName) {
        String encodedCardName = getEncodedCardName(cardName);
        String url = "https://yugipedia.com/wiki/Card_Tips:" + encodedCardName;

        try {
            Document dom = Jsoup.parse(jsoupGet(url));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getCardTriviaByCardName(String cardName) {
        String encodedCardName = getEncodedCardName(cardName);
        String url = "https://yugipedia.com/wiki/Card_Trivia:" + encodedCardName;

        try {
            Document dom = Jsoup.parse(jsoupGet(url));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getEncodedCardName(String cardName) {
        return cardName.replaceAll("%", "%25")
                       .replaceAll("'", "%27")
                       .replaceAll("\\?", "%3F")
                       .replaceAll(" ", "_");
    }

    private String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return YugiohWikiUtil.getCleanedHtml(content, isTipsPage, false);
    }

    public Map<String, String> getRulingMap() throws IOException, JSONException {
        return getCategoryMap("Card_Rulings", "Card Rulings:", null, new ConcurrentHashMap<>());
    }

    public Map<String, String> getTipMap() throws IOException, JSONException {
        return getCategoryMap("Card_Tips", "Card Tips:", null, new ConcurrentHashMap<>());
    }

    public Map<String, String> getTriviaMap() throws IOException, JSONException {
        return getCategoryMap("Card_Trivia", "Card Trivia:", null, new ConcurrentHashMap<>());
    }

    private Map<String, String> getCategoryMap(String categoryName, String titleCategorySubstring, String cmcontinue, Map<String, String> pageIdMap) throws JSONException, IOException {
        String url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers&cmtitle=Category:" + categoryName + "&cmlimit=500";

        if (cmcontinue != null) {
            url = url + "&cmcontinue=" + cmcontinue;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONObject("query").getJSONArray("categorymembers");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject articleInfo = myArray.getJSONObject(i);
            String title = articleInfo.getString("title");
            if (!title.contains(titleCategorySubstring)) {
                continue;
            }

            String cardName = title.substring(titleCategorySubstring.length());
            if (cardName.trim().endsWith("(temp)")) {
                continue;
            }
            if (!pageIdMap.containsKey(cardName)) {
                String pageid = articleInfo.getLong("pageid") + "";
                pageIdMap.put(cardName, pageid);
            }
        }

        if (myJSON.has("continue")) {
            String nextCmcontinue = myJSON.getJSONObject("continue").getString("cmcontinue");
            return getCategoryMap(categoryName, titleCategorySubstring, nextCmcontinue, pageIdMap);
        }
        else {
            return pageIdMap;
        }
    }

    @Override
    public Map<String, String> getCardMap(boolean isTcg) throws IOException, JSONException {
        return getCardMap(null, new HashMap<>(8192), isTcg);
    }

    private Map<String, String> getCardMap(String cmcontinue, Map<String, String> cardMap, boolean isTcg) throws JSONException, IOException {
        String url;
        if (isTcg) {
            url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers&cmtitle=Category:TCG_cards&cmlimit=500";
        }
        else {
            url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers&cmtitle=Category:OCG_cards&cmlimit=500";
        }

        if (cmcontinue != null) {
            url = url + "&cmcontinue=" + cmcontinue;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONObject("query").getJSONArray("categorymembers");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject articleInfo = myArray.getJSONObject(i);
            String title = articleInfo.getString("title");

            if (title.trim().endsWith("(temp)")) {
                continue;
            }

            if (!cardMap.containsKey(title)) {
                String pageid = articleInfo.getLong("pageid") + "";
                cardMap.put(title, pageid);
            }
        }

        if (myJSON.has("continue")) {
            String nextCmcontinue = myJSON.getJSONObject("continue").getString("cmcontinue");
            return getCardMap(nextCmcontinue, cardMap, isTcg);
        }
        else {
            return cardMap;
        }
    }

    @Override
    public Map<String, String> getBoosterMap(boolean isTcg) throws JSONException, IOException {
        return getBoosterMap(null, new HashMap<>(), isTcg);
    }

    private Map<String, String> getBoosterMap(String cmcontinue, Map<String, String> boosterMap, boolean isTcg) throws JSONException, IOException {
        String url;
        if (isTcg) {
            url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers&cmtitle=Category:TCG_Booster_Packs&cmlimit=500";
        }
        else {
            url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers&cmtitle=Category:OCG_Booster_Packs&cmlimit=500";
        }

        if (cmcontinue != null) {
            url = url + "&cmcontinue=" + cmcontinue;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONObject("query").getJSONArray("categorymembers");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject articleInfo = myArray.getJSONObject(i);
            String title = articleInfo.getString("title");

            if (title.trim().endsWith("(temp)")) {
                continue;
            }

            if (!boosterMap.containsKey(title)) {
                String pageid = articleInfo.getLong("pageid") + "";
                boosterMap.put(title, pageid);
            }
        }

        if (myJSON.has("continue")) {
            String nextCmcontinue = myJSON.getJSONObject("continue").getString("cmcontinue");
            return getBoosterMap(nextCmcontinue, boosterMap, isTcg);
        }
        else {
            return boosterMap;
        }
    }

    @Override
    public Booster getBooster(String boosterName, String boosterLink) throws IOException {
        return null;
    }

    @Override
    public Card getCard(String cardName, String pageid) throws IOException {
        String cardUrl = "https://yugipedia.com/?curid=" + pageid;
        Document mainDom = Jsoup.parse(jsoupGet(cardUrl));
        YugipediaCardParser parser = new YugipediaCardParser(cardName, mainDom);
        Card card = parser.parse();

        return card;
    }

    @Override
    public String getRuling(String cardName) {
        String pageid = yugipediaRulingMap.get(cardName);
        return getPageContentGeneric(pageid);
    }

    @Override
    public String getTips(String cardName) {
        String pageid = yugipediaTipMap.get(cardName);
        return getPageContentGeneric(pageid);
    }

    @Override
    public String getTrivia(String cardName) {
        String pageid = yugipediaTriviaMap.get(cardName);
        return getPageContentGeneric(pageid);
    }

    private String getPageContentGeneric(String pageid) {
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugipedia.com/?curid=" + pageid));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            /* do nothing */
            return null;
        }
    }
}
