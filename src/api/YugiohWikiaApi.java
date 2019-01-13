package api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static parser.Util.jsoupGet;

public class YugiohWikiaApi {
    public Map<String, String> getCardMap(boolean isTcg) throws IOException, JSONException {
        return getCardMap(null, new HashMap<>(8192), isTcg);
    }

    private Map<String, String> getCardMap(String offset, Map<String, String> cardMap, boolean isTcg) throws JSONException, IOException {
        // this will return up to 5000 articles in the TCG_cards/OCG_cards category.
        // Note that this is not always up-to-date, as newly added articles may take
        // a day or two before showing up in here
        String url;
        if (isTcg) {
            url = "http://yugioh.wikia.com/api/v1/Articles/List?category=TCG_cards&limit=5000&namespaces=0";
        }
        else {
            url = "http://yugioh.wikia.com/api/v1/Articles/List?category=OCG_cards&limit=5000&namespaces=0";
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

    public Map<String, String> getBoosterMap(boolean isTcg) throws IOException, JSONException {
        return getBoosterMap(null, new HashMap<>(), isTcg);
    }

    private Map<String, String> getBoosterMap(String offset, Map<String, String> boosterMap, boolean isTcg) throws JSONException, IOException {
        String url;
        if (isTcg) {
            url = "http://yugioh.wikia.com/api/v1/Articles/List?category=TCG_Booster_Packs&limit=5000&namespaces=0";
        }
        else {
            url = "http://yugioh.wikia.com/api/v1/Articles/List?category=OCG_Booster_Packs&limit=5000&namespaces=0";
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
}
