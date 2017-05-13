import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parsing booster dom
 *
 * Created by Chin on 06-Feb-17.
 */
public class BoosterParser {
    private Element dom;
    private String boosterName;

    public BoosterParser(String boosterName, Document dom) {
        this.boosterName = boosterName;
        Element elem = dom.getElementById("mw-content-text");
        removeSupTag(elem);
        this.dom = elem;
    }

    public String getJapaneseReleaseDate() {
        return getReleaseDate("Japan");
    }

    public String getEnglishReleaseDate() {
        return getReleaseDate("North America");
    }

    public String getSouthKoreaReleaseDate() {
        return getReleaseDate("South Korea");
    }

    public String getWorldwideReleaseDate() {
        return getReleaseDate("Worldwide");
    }

    public String getReleaseDate(String type) {
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

    public String getImageLink() {
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

    private static void removeSupTag(Element elem) {
        Elements sups = elem.getElementsByTag("sup");
        for (Element e : sups) {
            e.remove();
        }
    }
}
