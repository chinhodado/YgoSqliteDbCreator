import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class MainParallel {
    private static List<String> cardList = new ArrayList<>(8192);
    private static Set<String> ocgCards = new HashSet<>(8192);
    private static Set<String> tcgCards = new HashSet<>(8192);
    private static Map<String, String[]> cardLinkTable = new HashMap<>(8192);

    private static List<String> boosterList = new ArrayList<>();
    private static Set<String> ocgBoosters = new HashSet<>();
    private static Set<String> tcgBoosters = new HashSet<>();
    private static Map<String, String[]> boosterLinkTable = new HashMap<>();

    private static PreparedStatement psParms;
    private static PreparedStatement psBoosterInsert;
    private static final AtomicInteger cardDoneCounter = new AtomicInteger();
    private static final AtomicInteger boosterDoneCounter = new AtomicInteger();
    private static int iteration = 0;

    // settings
    private static final boolean ENABLE_VERBOSE_LOG = false;
    private static final boolean ENABLE_TRIVIA = true;
    private static final boolean ENABLE_TIPS = true;
    private static final int NUM_THREAD = 8;

    public static void main (String args[]) throws InterruptedException, ExecutionException, JSONException, IOException, ClassNotFoundException, SQLException {
        logLine("Initializing TCG card list");
        initializeCardList(null, true);
        logLine("Initializing OCG card list");
        initializeCardList(null, false);
        logLine("Initializing TCG booster list");
        initializeBoosterList(null, true);
        logLine("Initializing OCG booster list");
        initializeBoosterList(null, false);

        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        Statement stmt = connection.createStatement();
        String sql = "CREATE TABLE Card " +
                "( name              TEXT NOT NULL, " +
                "  attribute         TEXT, " +      //             "Attribute"
                "  types             TEXT, " +      //             "Types" or "Type"
                "  level             INTEGER, " +   //             "Level"
                "  atk               INTEGER, " +   //             "ATK/DEF"
                "  def               INTEGER, " +   //             "ATK/DEF"
                "  cardnum           TEXT, " +      //             "Card Number"
                "  passcode          TEXT, " +      //             "Passcode"
                "  effectTypes       TEXT, " +      //             "Card effect types"
                "  materials         TEXT, " +      // synchro     "Materials"
                "  fusionMaterials   TEXT, " +      // fusion      "Fusion Material"
                "  rank              INTEGER, " +   // xyz         "Rank"
                "  ritualSpell       TEXT, " +      // ritual      "Ritual Spell Card required"
                "  pendulumScale     INTEGER, " +   // pendulum    "Pendulum Scale"
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
                   "  tcgAdvStatus TEXT, " +
                   "  tcgTrnStatus TEXT, " +
                   "  ocgOnly   INTEGER, " +
                   "  tcgOnly   INTEGER, " +
                   "  img TEXT) ";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE metadata (dataCreated TEXT NOT NULL)";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE Booster (name TEXT NOT NULL, enReleaseDate TEXT, jpReleaseDate TEXT, imgSrc TEXT)";
        stmt.executeUpdate(sql);

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        psParms = connection.prepareStatement("INSERT INTO metadata (dataCreated) VALUES (?)");
        psParms.setString(1, date);
        psParms.executeUpdate();

        stmt.executeUpdate("CREATE INDEX name_idx ON Card (name)");
        stmt.executeUpdate("CREATE INDEX attribute_idx ON Card (attribute)");
        stmt.executeUpdate("CREATE INDEX level_idx ON Card (level)");
        stmt.executeUpdate("CREATE INDEX rank_idx ON Card (rank)");
        stmt.executeUpdate("CREATE INDEX pendulumScale_idx ON Card (pendulumScale)");
        stmt.executeUpdate("CREATE INDEX atk_idx ON Card (atk)");
        stmt.executeUpdate("CREATE INDEX def_idx ON Card (def)");
        stmt.executeUpdate("CREATE INDEX property_idx ON Card (property)");
        stmt.executeUpdate("CREATE INDEX ocgStatus_idx ON Card (ocgStatus)");
        stmt.executeUpdate("CREATE INDEX tcgAdvStatus_idx ON Card (tcgAdvStatus)");
        stmt.executeUpdate("CREATE INDEX tcgTrnStatus_idx ON Card (tcgTrnStatus)");
        stmt.executeUpdate("CREATE INDEX ocgOnly_idx ON Card (ocgOnly)");
        stmt.executeUpdate("CREATE INDEX tcgOnly_idx ON Card (tcgOnly)");
        stmt.executeUpdate("CREATE INDEX booster_name_idx ON Booster (name)");

        psParms = connection.prepareStatement(
                "INSERT INTO Card (name, attribute, types, level, atk, def, cardnum, passcode, " +
                "effectTypes, materials, fusionMaterials, rank, ritualSpell, " +
                "pendulumScale, property, summonedBy, limitText, synchroMaterial, ritualMonster, " +
                "ruling, tips, trivia, lore, ocgStatus, tcgAdvStatus, tcgTrnStatus, ocgOnly, tcgOnly, img) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        psBoosterInsert = connection.prepareStatement(
                "INSERT INTO Booster (name, enReleaseDate, jpReleaseDate, imgSrc) " +
                        "VALUES (?,?,?,?)");

        logLine("Getting and processing Yugioh Wikia articles using " + NUM_THREAD + " threads.");
        Scanner in = new Scanner(System.in);

        logLine("Processing card list");
        List<String> workList = cardList;
        int totalCards = cardList.size();
        while (!workList.isEmpty()) {
            iteration++;
            workList = doWork(workList, MainParallel::processCard, totalCards, cardDoneCounter);

            if (!workList.isEmpty()) {
                System.out.println("Do you want to retry the " + workList.size() + " cards with error? (y/n)");
                String s = in.next();
                if (s.equals("n")) {
                    break;
                }
            }
        }

        logLine("Processing booster list");
        workList = boosterList;
        int totalBoosters = boosterList.size();
        while (!workList.isEmpty()) {
            iteration++;
            workList = doWork(workList, MainParallel::processBooster, totalBoosters, boosterDoneCounter);

            if (!workList.isEmpty()) {
                System.out.println("Do you want to retry the " + workList.size() + " boosters with error? (y/n)");
                String s = in.next();
                if (s.equals("n")) {
                    break;
                }
            }
        }

        System.out.println();
        logLine("Completed all iterations.");

        // Dump the database contents to a file
        stmt.executeUpdate("backup to ygo.db");
        stmt.close();

        connection.close();
        logLine("Saved to ygo.db successfully. Everything done.");
    }

    private static List<String> doWork(List<String> workList, Work workFunction, int totalWorkSize, AtomicInteger doneCounter) throws InterruptedException {
        System.out.println();
        int size = workList.size();
        logLine("Executing iteration " + iteration + ", items left: " + size);
        final List<String> errorList = new CopyOnWriteArrayList<>();
        if (size == 0) {
            return errorList;
        }

        int numThread = NUM_THREAD;

        if (numThread >= size) {
            numThread = 1;
        }

        // partitioning
        final int CHUNK_SIZE = size / numThread;
        final int LAST_CHUNK = size - (numThread - 1) * CHUNK_SIZE; // last chunk can be a bit bigger

        List<List<String>> parts = new ArrayList<>();
        for (int i = 0; i < size - LAST_CHUNK; i += CHUNK_SIZE) {
            parts.add(new ArrayList<>(
                    workList.subList(i, i + CHUNK_SIZE))
            );
        }

        parts.add(new ArrayList<>(
                workList.subList(size - LAST_CHUNK, size))
        );

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < numThread; i++) {
            List<String> part = parts.get(i);
            Runnable r = () -> {
                for (String card : part) {
                    try {
                        workFunction.processItem(card, iteration >= 2 && iteration <= 10, doneCounter);
                    } catch (Exception e) {
                        errorList.add(card);
                    }
                }
            };
            Thread thread = new Thread(r);
            thread.start();
            threadList.add(thread);
        }

        Thread logThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.print("\r");
                int done = doneCounter.get();
                int error = errorList.size();
                double percentage = (double)done / totalWorkSize * 100;
                System.out.print("Completed: " + done + "/" + totalWorkSize + "(" + percentage + "%), error: " + error + "                  ");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        logThread.start();

        for(Thread t : threadList) {
            t.join();
        }

        logThread.interrupt();
        logThread.join();

        if (!errorList.isEmpty()) {
            System.out.println("\nError remaining: ");
            for (String s : errorList) {
                System.out.print(s + " | ");
            }
            System.out.println();
        }

        return errorList;
    }

    private static void processBooster(String boosterName, boolean purgePage, AtomicInteger doneCounter) throws IOException, SQLException {
        String enReleaseDate, jpReleaseDate, imgSrc;

        String boosterLink = boosterLinkTable.get(boosterName)[0];
        String boosterUrl = "http://yugioh.wikia.com" + boosterLink;

        if (purgePage) {
            boosterUrl += "?action=purge";
        }

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + boosterName + "'s general info");
        Document mainDom = Jsoup.parse(jsoupGet(boosterUrl));
        BoosterParser parser = new BoosterParser(boosterName, mainDom);

        enReleaseDate = parser.getEnglishReleaseDate();
        jpReleaseDate = parser.getJapaneseReleaseDate();
        imgSrc = getShortenedImageLink(parser.getImageLink());

        psBoosterInsert.setString(1, boosterName);
        psBoosterInsert.setString(2, enReleaseDate);
        psBoosterInsert.setString(3, jpReleaseDate);
        psBoosterInsert.setString(4, imgSrc);

        psBoosterInsert.executeUpdate();
        doneCounter.incrementAndGet();
    }

    /**
     *
     * @param cardName The card name
     * @param purgePage If true, the article page will be purged on the server.
     *                  Useful for dealing with the "blank page" issue.
     * @throws IOException when something's wrong with fetching card info from the net
     * @throws SQLException when something's wrong with inserting the card into the database
     */
    private static void processCard(String cardName, boolean purgePage, AtomicInteger doneCounter) throws IOException, SQLException {
        String attribute = "", types = "", level = "", atk = "", def = "", cardnum = "", passcode = "",
                effectTypes = "", materials = "", fusionMaterials = "", rank = "", ritualSpell = "",
                pendulumScale = "", property = "", summonedBy = "", limitText = "", synchroMaterial = "", ritualMonster = "",
                ruling = "", tips = "", trivia = "", lore = "", ocgStatus = "", tcgAdvStatus = "", tcgTrnStatus = "",
                ocgOnly = "", tcgOnly = "", img = "";

        String cardLink = cardLinkTable.get(cardName)[0];
        String cardUrl = "http://yugioh.wikia.com" + cardLink;

        if (purgePage) {
            cardUrl += "?action=purge";
        }

        try {
            if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s ruling");
            Document dom = Jsoup.parse(jsoupGet("http://yugioh.wikia.com/wiki/Card_Rulings:" + cardLink.substring(6)));
            ruling = getCardInfoGeneric(dom, false);
        }
        catch (Exception e) { /* do nothing */ }

        if (ENABLE_TIPS) {
            try {
                if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s tips");
                Document dom = Jsoup.parse(jsoupGet("http://yugioh.wikia.com/wiki/Card_Tips:" + cardLink.substring(6)));
                tips = getCardInfoGeneric(dom, true);
            }
            catch (Exception e) { /* do nothing */ }
        }

        if (ENABLE_TRIVIA) {
            try {
                if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s trivia");
                Document dom = Jsoup.parse(jsoupGet("http://yugioh.wikia.com/wiki/Card_Trivia:" + cardLink.substring(6)));
                trivia = getCardInfoGeneric(dom, false);
            }
            catch (Exception e) { /* do nothing */ }
        }

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s general info");
        Document mainDom = Jsoup.parse(jsoupGet(cardUrl));
        Elements rows = mainDom.getElementsByClass("cardtable").first().getElementsByClass("cardtablerow");

        try {
            Element imgAnchor = mainDom.getElementsByClass("cardtable-cardimage").first().getElementsByClass("image-thumbnail").first();
            String imgUrl = imgAnchor.attr("href");
            img = getShortenedImageLink(imgUrl);
        }
        catch (Exception e) {
            /* do nothing */
        }

        // first row is "Attribute" for monster, "Type" for spell/trap and "Types" for token
        boolean foundFirstRow = false;
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Element header = row.getElementsByClass("cardtablerowheader").first();
            if (header == null) continue;
            String headerText = header.text();
            if (!foundFirstRow && !headerText.equals("Attribute") && !headerText.equals("Type") && !headerText.equals("Types")) {
                continue;
            }
            if (headerText.equals("Other card information") || headerText.equals("External links")) {
                // we have reached the end for some reasons, exit now
                break;
            }
            else {
                foundFirstRow = true;
                String data = row.getElementsByClass("cardtablerowdata").first().text().trim();
                switch (headerText) {
                    case "Attribute"                    : attribute       = data; break;
                    case "Types"                        : types           = data; break;
                    case "Type"                         : types           = data; break;
                    case "Level"                        : level           = data; break;
                    case "ATK / DEF"                    : {
                    	atk = data.split(" / ")[0];
                    	def = data.split(" / ")[1];
                    	break;
                	}
                    case "Card Number"                  : cardnum         = data; break;
                    case "Passcode"                     : passcode        = data; break;
                    case "Card effect types"            : effectTypes     = data; break;
                    case "Materials"                    : materials       = data; break;
                    case "Fusion Material"              : fusionMaterials = data; break;
                    case "Rank"                         : rank            = data; break;
                    case "Ritual Spell Card required"   : ritualSpell     = data; break;
                    case "Pendulum Scale"               : pendulumScale   = data; break;
                    case "Property"                     : property        = data; break;
                    case "Summoned by the effect of"    : summonedBy      = data; break;
                    case "Limitation Text"              : limitText       = data; break;
                    case "Synchro Material"             : synchroMaterial = data; break;
                    case "Ritual Monster required"      : ritualMonster   = data; break;
                    case "Statuses":
                        try {
                            String rowspan = header.attr("rowspan");
                            int numStatusRow;
                            if (rowspan != null && !rowspan.equals("")) {
                                numStatusRow = Integer.parseInt(rowspan);
                            }
                            else {
                                numStatusRow = 1;
                            }

                            for (int r = 0; r < numStatusRow; r++) {
                                Element statusRow = rows.get(i + r);
                                String statusRowData = statusRow.getElementsByClass("cardtablerowdata").first().text().trim();
                                String status;
                                if (statusRowData.contains("Not yet released")) {
                                    status = "Not yet released";
                                }
                                else {
                                    status = statusRowData.split(" ")[0];
                                }

                                if (status.equals("Unlimited")) {
                                    status = "U";
                                }

                                if (numStatusRow == 1) {
                                    ocgStatus = status;
                                    tcgAdvStatus = status;
                                    tcgTrnStatus = status;
                                }
                                else {
                                    if (statusRowData.contains("OCG")) {
                                        ocgStatus = status;
                                    }

                                    if (statusRowData.contains("Advanced")) {
                                        tcgAdvStatus = status;
                                    }

                                    if (statusRowData.contains("Traditional")) {
                                        tcgTrnStatus = status;
                                    }
                                }
                            }

                            // skip through the status rows
                            i = i + numStatusRow - 1;
                        }
                        catch (Exception e) {
                            System.out.println("Error getting status: " + cardName);
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("Attribute not found: " + headerText);
                        break;
                }

                // rely on the assumption that Statuses is always the last info row
                if (headerText.equals("Statuses")) {
                    break;
                }
            }
        }

        lore = getCardLore(mainDom);

        if (tcgCards.contains(cardName) && !ocgCards.contains(cardName)) {
            tcgOnly = "1";
        }

        if (ocgCards.contains(cardName) && !tcgCards.contains(cardName)) {
            ocgOnly = "1";
        }

        psParms.setString(1,  cardName);
        psParms.setString(2,  attribute);
        psParms.setString(3,  types);
        psParms.setString(4,  level);
        psParms.setString(5,  atk);
        psParms.setString(6,  def);
        psParms.setString(7,  cardnum);
        psParms.setString(8,  passcode);
        psParms.setString(9,  effectTypes);
        psParms.setString(10,  materials);
        psParms.setString(11, fusionMaterials);
        psParms.setString(12, rank);
        psParms.setString(13, ritualSpell);
        psParms.setString(14, pendulumScale);
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
        psParms.setString(27, ocgOnly);
        psParms.setString(28, tcgOnly);
        psParms.setString(29, img);

        psParms.executeUpdate();
        doneCounter.incrementAndGet();
    }

    private static String getCardLore(Document dom) {
        Element effectBox = dom.getElementsByClass("cardtablespanrow").first().getElementsByClass("navbox-list").first();
        String effect = getCleanedHtml(effectBox, false);

        // turn <dl> into <p> and <dt> into <b>
        effect = effect.replace("<dl", "<p").replace("dl>", "p>").replace("<dt", "<b").replace("dt>", "b>");
        return effect;
    }

    private static String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return getCleanedHtml(content, isTipsPage);
    }

    private static String getCleanedHtml(Element content, boolean isTipPage) {
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
        return text;
    }

    private static final Pattern IMG_URL_PATTERN =
            Pattern.compile("http(s?)://vignette(\\d)\\.wikia\\.nocookie\\.net/yugioh/images/./(.*?)/(.*?)/");
    private static String getShortenedImageLink(String imgUrl) {
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
        String jsonString = jsoupGet(url);

        JSONObject myJSON = new JSONObject(jsonString);
        JSONArray myArray = myJSON.getJSONArray("items");
        for (int i = 0; i < myArray.length(); i++) {
            String cardName = myArray.getJSONObject(i).getString("title");
            if (cardName.trim().endsWith("(temp)")) {
                continue;
            }
            if (!cardLinkTable.containsKey(cardName)) {
                cardList.add(cardName);
                String[] tmp = {myArray.getJSONObject(i).getString("url")}; // TODO: no need for array
                       // myArray.getJSONObject(i).getString("id")};
                cardLinkTable.put(cardName, tmp);
            }

            if (isTcg) {
                tcgCards.add(cardName);
            }
            else {
                ocgCards.add(cardName);
            }
        }

        if (myJSON.has("offset")) {
            initializeCardList((String) myJSON.get("offset"), isTcg);
        }
    }

    private static void initializeBoosterList(String offset, boolean isTcg) throws InterruptedException, ExecutionException, JSONException, IOException {
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
            String boosterName = myArray.getJSONObject(i).getString("title");
            if (boosterName.trim().endsWith("(temp)")) {
                continue;
            }
            if (!boosterLinkTable.containsKey(boosterName)) {
                boosterList.add(boosterName);
                String[] tmp = {myArray.getJSONObject(i).getString("url")}; // TODO: no need for array
                // myArray.getJSONObject(i).getString("id")};
                boosterLinkTable.put(boosterName, tmp);
            }

            if (isTcg) {
                tcgBoosters.add(boosterName);
            }
            else {
                ocgBoosters.add(boosterName);
            }
        }

        if (myJSON.has("offset")) {
            initializeBoosterList((String) myJSON.get("offset"), isTcg);
        }
    }

    private static void logLine(String txt) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + ": " + txt);
    }

    interface Work {
        void processItem(String itemName, boolean purgePage, AtomicInteger doneCounter) throws IOException, SQLException;
    }

    private static String jsoupGet(String url) throws IOException {
        String content = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .referrer("http://www.google.com")
                .timeout(5 * 1000).ignoreContentType(true).execute().body();
        return content;
    }
}
