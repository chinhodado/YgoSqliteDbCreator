package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chin on 13-May-17.
 */
public class Util {
    public static String getCleanedHtml(Element content, boolean isTipPage, boolean rawText) {
        Elements navboxes = content.select("table.navbox");
        if (!navboxes.isEmpty()) {navboxes.first().remove();} // remove the navigation box

        content.select("script").remove();               // remove <script> tags
        content.select("noscript").remove();             // remove <noscript> tags
        content.select("#toc").remove();                 // remove the table of content
        content.select("sup").remove();                  // remove the sup tags
        content.select("#References").remove();          // remove the reference header
        content.select(".references").remove();          // remove the references section
        content.select(".mbox-image").remove();          // remove the image in the "previously official ruling" box

        // remove the "Previously Official Rulings" notice
        Elements tables = content.getElementsByTag("table");
        for (Element table : tables) {
            if (table.text().startsWith("These TCG rulings were issued by Upper Deck Entertainment")) {
                // TODO: may want to put a placeholder here so we know to put it back in later
                table.remove();
            }
        }

        if (isTipPage) {
            // remove the "lists" tables
            boolean foundListsHeader = false;
            Elements children = content.select("#mw-content-text").first().children();
            for (Element child : children) {
                if ((child.tagName().equals("h2") || child.tagName().equals("h3")) && child.text().contains("List")) {
                    foundListsHeader = true;
                    child.remove();
                }
                else if (foundListsHeader && (child.tagName().equals("h2") || child.tagName().equals("h3"))) {
                    break;
                }
                else if (foundListsHeader) {
                    child.remove();
                }
            }
        }

        removeComments(content);                         // remove comments
        removeAttributes(content);                       // remove all attributes. Has to be at the end, otherwise can't grab id, etc.
        removeEmptyTags(content);                        // remove all empty tags

        // convert to text
        String text = content.html();

        // remove useless tags
        text = text.replace("<span>", "").replace("</span>", "").replace("<a>", "").replace("</a>", "");
        if (rawText) {
            text = Jsoup.parse(text).text();
        }
        return text;
    }

    private static void removeComments(Node node) {
        for (int i = 0; i < node.childNodes().size();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }

    private static void removeAttributes(Element doc) {
        Elements el = doc.getAllElements();
        for (Element e : el) {
            Attributes at = e.attributes();
            for (Attribute a : at) {
                e.removeAttr(a.getKey());
            }
        }
    }

    private static void removeEmptyTags(Element doc) {
        for (Element element : doc.select("*")) {
            if (!element.hasText() && element.isBlock()) {
                element.remove();
            }
        }
    }

    public static void logLine(String txt) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + ": " + txt);
    }

    public static void removeSupTag(Element elem) {
        Elements sups = elem.getElementsByTag("sup");
        for (Element e : sups) {
            e.remove();
        }
    }

    private static final Pattern IMG_URL_PATTERN =
            Pattern.compile("http(s?)://vignette(\\d)\\.wikia\\.nocookie\\.net/yugioh/images/./(.*?)/(.*?)/");
    public static String getShortenedImageLink(String imgUrl) {
        try {
            Matcher m = IMG_URL_PATTERN.matcher(imgUrl);
            m.find();
            String img = m.group(2) + m.group(3) + m.group(4);
            return img;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String jsoupGet(String url) throws IOException {
        String content = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .referrer("http://www.google.com")
                .timeout(5 * 1000).ignoreContentType(true).execute().body();
        return content;
    }
}
