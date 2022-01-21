package com.chin.ygowikitool.api;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;

import com.chin.ygowikitool.entity.Booster;
import com.chin.ygowikitool.entity.Card;

public interface YugiohApi {
    Map<String, String> getCardMap(boolean isTcg) throws IOException, JSONException;

    Map<String, String> getBoosterMap(boolean isTcg) throws IOException, JSONException;

    Booster getBooster(String boosterName, String boosterLink) throws IOException;

    Card getCard(String cardName, String cardLink) throws IOException;

    String getRuling(String cardLink);

    String getTips(String cardLink);

    String getTrivia(String cardLink);
}
