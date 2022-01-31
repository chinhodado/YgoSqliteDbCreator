package com.chin.ygowikitool.entity;

import static com.chin.ygowikitool.parser.YugiohWikiUtil.logLine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Chin on 22-May-17.
 */
public class Booster {
    public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
    private static final DateFormat[] DATE_FORMATS = new DateFormat[] {
            DEFAULT_DATE_FORMAT,
            new SimpleDateFormat("MMMM, yyyy", Locale.ENGLISH),
            new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH),
            new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH),
            new SimpleDateFormat("MMMM d yyyy", Locale.ENGLISH),
    };

    private static final Date DEFAULT_DATE_OBJECT = new Date(70, Calendar.JANUARY, 1);

    private String boosterName;
    private String enReleaseDate;
    private String jpReleaseDate;
    private String skReleaseDate;
    private String worldwideReleaseDate;
    private String shortenedImgSrc;
    private String fullImgSrc;

    private String introText;
    private String featureText;
    private Map<String, Card> cardMap;

    private Date enReleaseDateObject;
    private Date jpReleaseDateObject;
    private String url;

    public String getBoosterName() {
        return boosterName;
    }

    public void setBoosterName(String boosterName) {
        this.boosterName = boosterName;
    }

    public String getEnReleaseDate() {
        return enReleaseDate;
    }

    public void setEnReleaseDate(String enReleaseDate) {
        this.enReleaseDate = enReleaseDate;
    }

    public String getJpReleaseDate() {
        return jpReleaseDate;
    }

    public void setJpReleaseDate(String jpReleaseDate) {
        this.jpReleaseDate = jpReleaseDate;
    }

    public String getSkReleaseDate() {
        return skReleaseDate;
    }

    public void setSkReleaseDate(String skReleaseDate) {
        this.skReleaseDate = skReleaseDate;
    }

    public String getWorldwideReleaseDate() {
        return worldwideReleaseDate;
    }

    public void setWorldwideReleaseDate(String worldwideReleaseDate) {
        this.worldwideReleaseDate = worldwideReleaseDate;
    }

    public String getShortenedImgSrc() {
        return shortenedImgSrc;
    }

    public void setShortenedImgSrc(String shortenedImgSrc) {
        this.shortenedImgSrc = shortenedImgSrc;
    }

    public String getFullImgSrc() {
        return fullImgSrc;
    }

    public void setFullImgSrc(String fullImgSrc) {
        this.fullImgSrc = fullImgSrc;
    }

    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public String getFeatureText() {
        return featureText;
    }

    public void setFeatureText(String featureText) {
        this.featureText = featureText;
    }

    public Map<String, Card> getCardMap() {
        return cardMap;
    }

    public void setCardMap(Map<String, Card> cardMap) {
        this.cardMap = cardMap;
    }

    public void parseEnReleaseDate() {
        if (enReleaseDate == null) {
            enReleaseDateObject = DEFAULT_DATE_OBJECT;
            return;
        }

        boolean parsed = false;
        for (DateFormat dateFormat : DATE_FORMATS) {
            try {
                this.enReleaseDateObject = dateFormat.parse(enReleaseDate);
                parsed = true;
                break;
            }
            catch (Exception e) {
                // do nothing
            }
        }

        if (!parsed) {
            logLine("Unable to parse date: " + enReleaseDate + " for " + boosterName);
            enReleaseDateObject = DEFAULT_DATE_OBJECT;
        }
    }

    public void parseJpReleaseDate() {
        if (jpReleaseDate == null) {
            jpReleaseDateObject = DEFAULT_DATE_OBJECT;
            return;
        }

        boolean parsed = false;
        for (DateFormat dateFormat : DATE_FORMATS) {
            try {
                this.jpReleaseDateObject = dateFormat.parse(jpReleaseDate);
                parsed = true;
                break;
            }
            catch (Exception e) {
                // do nothing
            }
        }

        if (!parsed) {
            logLine("Unable to parse date: " + jpReleaseDate + " for " + boosterName);
            jpReleaseDateObject = DEFAULT_DATE_OBJECT;
        }
    }

    public Date getEnReleaseDateObject() {
        return enReleaseDateObject == null? DEFAULT_DATE_OBJECT : enReleaseDateObject;
    }

    public Date getJpReleaseDateObject() {
        return jpReleaseDateObject == null? DEFAULT_DATE_OBJECT : jpReleaseDateObject;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
