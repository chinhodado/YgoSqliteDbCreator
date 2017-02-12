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
        try {
            Elements rows = dom.getElementsByClass("infobox").first().getElementsByTag("tr");
            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(i);
                if (row.text().equals("Release dates")) {
                    for (int j = i + 1; j < rows.size(); j++) {
                        Elements headers = rows.get(j).getElementsByTag("th");
                        if (headers.size() > 0 && headers.get(0).text().equals("Japanese")) {
                            String date = rows.get(j).getElementsByTag("td").first().text();
                            return date;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
        }

        return null;
    }

    public String getEnglishReleaseDate() {
        try {
            Elements rows = dom.getElementsByClass("infobox").first().getElementsByTag("tr");
            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(i);
                if (row.text().equals("Release dates")) {
                    String date = null;
                    for (int j = i + 1; j < rows.size(); j++) {
                        Elements headers = rows.get(j).getElementsByTag("th");
                        if (headers.size() > 0) {
                            Element header = headers.get(0);

                            if (header.text().startsWith("English")) {
                                date = rows.get(j).getElementsByTag("td").first().text();
                            }

                            if (header.text().equals("English (na)")){
                                return date;
                            }
                        }
                    }

                    return date;
                }
            }
        }
        catch (Exception e) {
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
