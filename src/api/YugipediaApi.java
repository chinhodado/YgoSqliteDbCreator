package api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import parser.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static parser.Util.jsoupGet;

public class YugipediaApi {

    public String getCardRuling(String pageid) {
        try {
            Document dom = Jsoup.parse(jsoupGet("https://yugipedia.com/?curid=" + pageid));
            return getCardInfoGeneric(dom, false);
        }
        catch (Exception e) {
            /* do nothing */
            return null;
        }
    }

    private String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return Util.getCleanedHtml(content, isTipsPage, false);
    }

    public Map<String, String> getRulingList() throws IOException, JSONException {
        return getRulingList(null, new HashMap<>());
    }

    private Map<String, String> getRulingList(String cmcontinue, Map<String, String> rulingMap) throws JSONException, IOException {
        String url = "http://yugipedia.com/api.php?action=query&format=json&list=categorymembers" +
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
            return getRulingList(nextCmcontinue, rulingMap);
        }
        else {
            return rulingMap;
        }
    }
}
