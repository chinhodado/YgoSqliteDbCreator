import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class Main {
    static ArrayList<String> cardList = new ArrayList<String>(8192);
    static Hashtable<String, String[]> cardLinkTable = new Hashtable<String, String[]>(8192);
    static Connection connection;
    static PreparedStatement psParms;

    // settings
    static boolean ENABLE_VERBOSE_LOG   = false;
    static boolean ENABLE_TRIVIA = true;
    static boolean ENABLE_TIPS   = true;

    public static void main (String args[]) throws InterruptedException, ExecutionException, JSONException, IOException, ClassNotFoundException, SQLException {
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing TCG card list");
        initializeCardList(null, true);
        System.out.println("Initializing OCG card list");
        initializeCardList(null, false);

        Statement stmt = null;

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ygo.db");

        stmt = connection.createStatement();
        String sql = "DROP TABLE IF EXISTS Card;";
        stmt.executeUpdate(sql);

        stmt = connection.createStatement();
        sql = "CREATE TABLE Card " +
                //    "(ID INT PRIMARY KEY     NOT NULL," +
                "( name              TEXT NOT NULL, " +
                "  attribute         TEXT, " +      //             "Attribute"
                "  types             TEXT, " +      //             "Types"
                "  level             TEXT, " +      //             "Level"
                "  atkdef            TEXT, " +      //             "ATK/DEF"
                "  cardnum           TEXT, " +      //             "Card Number"
                "  passcode          TEXT, " +      //             "Passcode"
                "  effectTypes       TEXT, " +      //             "Card effect types"
                "  materials         TEXT, " +      // synchro     "Materials"
                "  fusionMaterials   TEXT, " +      // fusion      "Fusion Material"
                "  rank              TEXT, " +      // xyz         "Rank"
                "  ritualSpell       TEXT, " +      // ritual      "Ritual Spell Card required"
                "  pendulumScale     TEXT, " +      // pendulum    "Pendulum Scale"
                "  type              TEXT, " +      // spell, trap "Type"
                "  property          TEXT, " +      // spell, trap "Property"
                "  summonedBy        TEXT, " +      // token       "Summoned by the effect of"
                "  limitText         TEXT, " +      //             "Limitation Text"
                "  synchroMaterial   TEXT, " +      //             "Synchro Material"
                "  ritualMonster     TEXT, " +      //             "Ritual Monster required"
                   // long stuffs...
                   "  ruling      TEXT, "  +
                   "  tips        TEXT, "  +
                   "  trivia      TEXT, "  +
                   "  lore        TEXT, "  +
                   "  ocgStatus   TEXT, "  +
                   "  tcgAdvStatus TEXT, "  +
                   "  tcgTrnStatus TEXT) ";
        stmt.executeUpdate(sql);
        stmt.close();

        psParms = connection.prepareStatement(
                "INSERT INTO Card (name, attribute, types, level, atkdef, cardnum, passcode, " +
                "effectTypes, materials, fusionMaterials, rank, ritualSpell, " +
                "pendulumScale, type, property, summonedBy, limitText, synchroMaterial, ritualMonster, " +
                "ruling, tips, trivia, lore, ocgStatus, tcgAdvStatus, tcgTrnStatus) " + 
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        int size = cardList.size();
        for (int i = 0; i < size; i++) {
            try {
                if (i % 120 == 0) System.out.println();
                System.out.print(".");
                processCard(i);
            } catch (Exception e) {
                System.out.println("Error: " + cardList.get(i));
                e.printStackTrace();
            }
        }

        connection.close();
        System.out.println("Done. Has the universe ended yet?");
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Time elapsed: " + elapsedTime / 1000 + "s");
    }

    /**
     *
     * @param i index
     * @throws IOException
     * @throws SQLException
     */
    static void processCard(int i) throws IOException, SQLException {
        String attribute = "", types = "", level = "", atkdef = "", cardnum = "", passcode = "",
                effectTypes = "", materials = "", fusionMaterials = "", rank = "", ritualSpell = "",
                pendulumScale = "", type = "", property = "", summonedBy = "", limitText = "", synchroMaterial = "", ritualMonster = "",
                ruling = "", tips = "", trivia = "", lore = "", ocgStatus = "", tcgAdvStatus = "", tcgTrnStatus = "";

        String cardName = cardList.get(i);
        String cardLink = cardLinkTable.get(cardName)[0];
        String cardUrl = "http://yugioh.wikia.com" + cardLink;

        try {
            if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s ruling");
            Document dom = Jsoup.parse(Jsoup.connect("http://yugioh.wikia.com/wiki/Card_Rulings:" + cardLink.substring(6))
                    .ignoreContentType(true).execute().body());
            ruling = getCardInfoGeneric(dom);
        }
        catch (Exception e) {}

        if (ENABLE_TIPS) {
            try {
                if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s tips");
                Document dom = Jsoup.parse(Jsoup.connect("http://yugioh.wikia.com/wiki/Card_Tips:" + cardLink.substring(6))
                        .timeout(5 * 1000).ignoreContentType(true).execute().body());
                tips = getCardInfoGeneric(dom);
            }
            catch (Exception e) {}
        }

        if (ENABLE_TRIVIA) {
            try {
                if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s trivia");
                Document dom = Jsoup.parse(Jsoup.connect("http://yugioh.wikia.com/wiki/Card_Trivia:" + cardLink.substring(6))
                        .timeout(5 * 1000).ignoreContentType(true).execute().body());
                trivia = getCardInfoGeneric(dom);
            }
            catch (Exception e) {}
        }

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s general info");
        Document mainDom = Jsoup.parse(Jsoup.connect(cardUrl).timeout(0).ignoreContentType(true).execute().body());
        Elements rows = mainDom.getElementsByClass("cardtable").first().getElementsByClass("cardtablerow");

        // first row is "Attribute" for monster, "Type" for spell/trap and "Types" for token
        boolean foundFirstRow = false;
        for (Element row : rows) {
            Element header = row.getElementsByClass("cardtablerowheader").first();
            if (header == null) continue;
            String headerText = header.text();
            if (!foundFirstRow && !headerText.equals("Attribute") && !headerText.equals("Type") && !headerText.equals("Types")) {
                continue;
            }
            if (headerText.equals("Other card information") || header.equals("External links")) {
                // we have reached the end for some reasons, exit now
                break;
            }
            else {
                foundFirstRow = true;
                String data = row.getElementsByClass("cardtablerowdata").first().text();
                switch (headerText) {
                    case "Attribute"                    : attribute       = data; break;
                    case "Types"                        : types           = data; break;
                    case "Level"                        : level           = data; break;
                    case "ATK/DEF"                      : atkdef          = data; break;
                    case "Card Number"                  : cardnum         = data; break;
                    case "Passcode"                     : passcode        = data; break;
                    case "Card effect types"            : effectTypes     = data; break;
                    case "Materials"                    : materials       = data; break;
                    case "Fusion Material"              : fusionMaterials = data; break;
                    case "Rank"                         : rank            = data; break;
                    case "Ritual Spell Card required"   : ritualSpell     = data; break;
                    case "Pendulum Scale"               : pendulumScale   = data; break;
                    case "Type"                         : type            = data; break;
                    case "Property"                     : property        = data; break;
                    case "Summoned by the effect of"    : summonedBy      = data; break;
                    case "Limitation Text"              : limitText       = data; break;
                    case "Synchro Material"             : synchroMaterial = data; break;
                    case "Ritual Monster required"      : ritualMonster   = data; break;
                    default:
                        System.out.println("Attribute not found: " + headerText);
                        break;
                }
                if (headerText.equals("Card effect types") || headerText.equals("Limitation Text")) {
                    break;
                }
            }
        }

        lore = getCardLore(mainDom);

        try {
            Elements statusRows = mainDom.getElementsByClass("cardtablestatuses").first().getElementsByTag("tr");
            Element statusRow = null;
            for (int j = 0; j < statusRows.size(); j++) {
                if (statusRows.get(j).text().equals("TCG/OCG statuses")) {
                    statusRow = statusRows.get(j + 1);
                    break;
                }
            }

            if (statusRow == null) {
                System.out.println("Status not found for: " + cardName);
            }
            else {
                Elements th = statusRow.getElementsByTag("th");
                Elements td = statusRow.getElementsByTag("td");
                for (int j = 0; j < th.size(); j++) {
                    String header = th.get(j).text();
                    String stt = td.get(j).text();
                    if (header.equals("OCG")) {
                        if (stt.equals("Unlimited")) {
                            ocgStatus = "U";
                        }
                        else {
                            ocgStatus = stt;
                        }
                    }
                    else if (header.equals("TCG Advanced")) {
                        if (stt.equals("Unlimited")) {
                            tcgAdvStatus = "U";
                        }
                        else {
                            tcgAdvStatus = stt;
                        }
                    }
                    else if (header.equals("TCG Traditional")) {
                        if (stt.equals("Unlimited")) {
                            tcgTrnStatus = "U";
                        }
                        else {
                            tcgTrnStatus = stt;
                        }
                    }
                    else {
                        System.out.println("Status not found: " + cardName);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error getting status: " + cardName);
            e.printStackTrace();
        }

        psParms.setString(1,  cardName);
        psParms.setString(2,  attribute);
        psParms.setString(3,  types);
        psParms.setString(4,  level);
        psParms.setString(5,  atkdef);
        psParms.setString(6,  cardnum);
        psParms.setString(7,  passcode);
        psParms.setString(8,  effectTypes);
        psParms.setString(9,  materials);
        psParms.setString(10, fusionMaterials);
        psParms.setString(11, rank);
        psParms.setString(12, ritualSpell);
        psParms.setString(13, pendulumScale);
        psParms.setString(14, type);
        psParms.setString(15, property);
        psParms.setString(16, summonedBy);
        psParms.setString(17, limitText);
        psParms.setString(18, synchroMaterial);
        psParms.setString(19, ritualMonster);
        psParms.setString(20, ruling);
        psParms.setString(21, tips);
        psParms.setString(22, trivia);
        psParms.setString(23, lore);
        psParms.setString(24, ocgStatus);
        psParms.setString(25, tcgAdvStatus);
        psParms.setString(26, tcgTrnStatus);

        psParms.executeUpdate();
    }

    private static String getCardLore(Document dom) {
        Element effectBox = dom.getElementsByClass("cardtablespanrow").first().getElementsByClass("navbox-list").first();
        String effect = getCleanedHtml(effectBox);

        // turn <dl> into <p> and <dt> into <b>
        effect = effect.replace("<dl", "<p").replace("dl>", "p>").replace("<dt", "<b").replace("dt>", "b>");
        return effect;
    }

    private static String getCardInfoGeneric(Document dom) {
        Element content = dom.getElementById("mw-content-text");
        String ruling = getCleanedHtml(content);
        return ruling;
    }

    static String getCleanedHtml(Element content) {
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

        removeComments(content);                         // remove comments
        removeAttributes(content);                       // remove all attributes. Has to be at the end, otherwise can't grab id, etc.
        removeEmptyTags(content);                        // remove all empty tags

        // convert to text
        String text = content.html();

        // remove useless tags
        text = text.replace("<span>", "").replace("</span>", "").replace("<a>", "").replace("</a>", "");
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

    private static void initializeCardList(String offset, boolean isTcg) throws InterruptedException, ExecutionException, JSONException, IOException {
        // this will return up to 5000 articles in the TCG_cards/OCG_cards category. Note that this is not always up-to-date,
        // as newly added articles may take a day or two before showing up in here
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
        String jsonString = Jsoup.connect(url).ignoreContentType(true).execute().body();

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONArray("items");
        for (int i = 0; i < myArray.length(); i++) {
            String cardName = myArray.getJSONObject(i).getString("title");
            if (!cardLinkTable.containsKey(cardName)) {
                cardList.add(cardName);
                String[] tmp = {myArray.getJSONObject(i).getString("url")}; // TODO: no need for array
                       // myArray.getJSONObject(i).getString("id")};
                cardLinkTable.put(cardName, tmp);
            }
        }

        if (myJSON.has("offset")) {
            initializeCardList((String) myJSON.get("offset"), isTcg);
        }
    }
}
