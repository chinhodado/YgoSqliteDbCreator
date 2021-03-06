package com.chin.ygowikitool.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.chin.ygowikitool.parser.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.chin.ygowikitool.parser.Util.jsoupGet;

public class YugipediaApi {

    public String getCardRulingByPageId(String pageid) {
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugipedia.com/?curid=" + pageid));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            /* do nothing */
            return null;
        }
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

    public String getEncodedCardName(String cardName) {
        return cardName.replaceAll("%", "%25")
                       .replaceAll("'", "%27")
                       .replaceAll("\\?", "%3F")
                       .replaceAll(" ", "_");
    }

    private String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return Util.getCleanedHtml(content, isTipsPage, false);
    }

    public Map<String, String> getRulingMap() throws IOException, JSONException {
        return getRulingMap(null, new HashMap<>());
    }

    private Map<String, String> getRulingMap(String cmcontinue, Map<String, String> rulingMap) throws JSONException, IOException {
        String url = "https://yugipedia.com/api.php?action=query&format=json&list=categorymembers" +
                "&cmtitle=Category:Card_Rulings&cmlimit=500";

        if (cmcontinue != null) {
            url = url + "&cmcontinue=" + cmcontinue;
        }
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONObject("query").getJSONArray("categorymembers");
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject articleInfo = myArray.getJSONObject(i);
            String title = articleInfo.getString("title");
            if (!title.contains("Card Rulings:")) {
                continue;
            }

            String cardName = title.substring("Card Rulings:".length());
            if (cardName.trim().endsWith("(temp)")) {
                continue;
            }
            if (!rulingMap.containsKey(cardName)) {
                String pageid = articleInfo.getLong("pageid") + "";
                rulingMap.put(cardName, pageid);
            }
        }

        if (myJSON.has("continue")) {
            String nextCmcontinue = myJSON.getJSONObject("continue").getString("cmcontinue");
            return getRulingMap(nextCmcontinue, rulingMap);
        }
        else {
            return rulingMap;
        }
    }
}
