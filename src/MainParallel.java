import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import entity.Booster;
import parser.BoosterParser;
import entity.Card;
import parser.CardParser;
import parser.Util;

import static parser.Util.jsoupGet;
import static parser.Util.logLine;

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
        parseArgs(args);
        outputArgs();
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
                "  realName          TEXT, " +      // for cards like Darkfire Soldier #1
                "  attribute         TEXT, " +      //              "Attribute"
                "  cardType          TEXT, " +      //              "Card type"
                "  types             TEXT, " +      //              "Types" or "Type"
                "  level             INTEGER, " +   //              "Level"
                "  atk               INTEGER, " +   //              "ATK/DEF"
                "  def               INTEGER, " +   //              "ATK/DEF"
                "  passcode          TEXT, " +      //              "Passcode"
                "  effectTypes       TEXT, " +      //              "Card effect types"
                "  materials         TEXT, " +      // synchro      "Materials"
                "  fusionMaterials   TEXT, " +      // fusion       "Fusion Material"
                "  rank              INTEGER, " +   // xyz          "Rank"
                "  ritualSpell       TEXT, " +      // ritual       "Ritual Spell Card required"
                "  pendulumScale     INTEGER, " +   // pendulum     "Pendulum Scale"
                "  linkMarkers       TEXT, " +      // link         "Link Markers"
                "  link              INTEGER, " +   // link         "ATK / LINK"
                "  property          TEXT, " +      // spell, trap  "Property"
                "  summonedBy        TEXT, " +      // token        "Summoned by the effect of"
                "  limitText         TEXT, " +      //              "Limitation Text"
                "  synchroMaterial   TEXT, " +      //              "Synchro Material"
                "  ritualMonster     TEXT, " +      // ritual spell "Ritual Monster required"
                   // long stuffs...
                   "  ruling      TEXT, "  +
                   "  tips        TEXT, "  +
                   "  trivia      TEXT, "  +
                   "  lore        TEXT, "  +
                   "  archetype   TEXT, "  +
                   "  ocgStatus   TEXT, "  +
                   "  tcgAdvStatus TEXT, " +
                   "  tcgTrnStatus TEXT, " +
                   "  ocgOnly   TEXT, " +
                   "  tcgOnly   TEXT, " +
                   "  img TEXT, " +
                   "  url TEXT) ";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE metadata (dataCreated TEXT NOT NULL)";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE Booster (name TEXT NOT NULL, enReleaseDate TEXT, jpReleaseDate TEXT, " +
                "skReleaseDate TEXT, worldwideReleaseDate TEXT, imgSrc TEXT)";
        stmt.executeUpdate(sql);

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        psParms = connection.prepareStatement("INSERT INTO metadata (dataCreated) VALUES (?)");
        psParms.setString(1, date);
        psParms.executeUpdate();

        stmt.executeUpdate("CREATE INDEX name_idx ON Card (name)");
        stmt.executeUpdate("CREATE INDEX realName_idx ON Card (realName)");
        stmt.executeUpdate("CREATE INDEX type_idx ON Card (cardType)");
        stmt.executeUpdate("CREATE INDEX attribute_idx ON Card (attribute)");
        stmt.executeUpdate("CREATE INDEX level_idx ON Card (level)");
        stmt.executeUpdate("CREATE INDEX rank_idx ON Card (rank)");
        stmt.executeUpdate("CREATE INDEX link_idx ON Card (link)");
        stmt.executeUpdate("CREATE INDEX pendulumScale_idx ON Card (pendulumScale)");
        stmt.executeUpdate("CREATE INDEX atk_idx ON Card (atk)");
        stmt.executeUpdate("CREATE INDEX def_idx ON Card (def)");
        stmt.executeUpdate("CREATE INDEX property_idx ON Card (property)");
        stmt.executeUpdate("CREATE INDEX archetype_idx ON Card (archetype)");
        stmt.executeUpdate("CREATE INDEX ocgStatus_idx ON Card (ocgStatus)");
        stmt.executeUpdate("CREATE INDEX tcgAdvStatus_idx ON Card (tcgAdvStatus)");
        stmt.executeUpdate("CREATE INDEX tcgTrnStatus_idx ON Card (tcgTrnStatus)");
        stmt.executeUpdate("CREATE INDEX ocgOnly_idx ON Card (ocgOnly)");
        stmt.executeUpdate("CREATE INDEX tcgOnly_idx ON Card (tcgOnly)");
        stmt.executeUpdate("CREATE INDEX booster_name_idx ON Booster (name)");

        psParms = connection.prepareStatement(
                "INSERT INTO Card (name, realName, attribute, cardType, types, level, atk, def, passcode, " +
                "effectTypes, materials, fusionMaterials, rank, ritualSpell, " +
                "pendulumScale, linkMarkers, link, property, summonedBy, limitText, synchroMaterial, ritualMonster, " +
                "ruling, tips, trivia, lore, archetype, ocgStatus, tcgAdvStatus, tcgTrnStatus, ocgOnly, tcgOnly, img) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        psBoosterInsert = connection.prepareStatement(
                "INSERT INTO Booster (name, enReleaseDate, jpReleaseDate, skReleaseDate, worldwideReleaseDate, imgSrc) " +
                        "VALUES (?,?,?,?,?,?)");

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
                        workFunction.processItem(card, doneCounter);
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

    private static void processBooster(String boosterName, AtomicInteger doneCounter) throws IOException, SQLException {
        String boosterLink = boosterLinkTable.get(boosterName)[0];
        String boosterUrl = "http://yugioh.wikia.com" + boosterLink;

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + boosterName + "'s general info");
        Document mainDom = Jsoup.parse(jsoupGet(boosterUrl));
        BoosterParser parser = new BoosterParser(boosterName, mainDom);
        Booster booster = parser.parse();

        psBoosterInsert.setString(1, boosterName);
        psBoosterInsert.setString(2, booster.getEnReleaseDate());
        psBoosterInsert.setString(3, booster.getJpReleaseDate());
        psBoosterInsert.setString(4, booster.getSkReleaseDate());
        psBoosterInsert.setString(5, booster.getWorldwideReleaseDate());
        psBoosterInsert.setString(6, booster.getImgSrc());

        psBoosterInsert.executeUpdate();
        doneCounter.incrementAndGet();
    }

    /**
     *
     * @param cardName The card name
     * @throws IOException when something's wrong with fetching card info from the net
     * @throws SQLException when something's wrong with inserting the card into the database
     */
    private static void processCard(String cardName, AtomicInteger doneCounter) throws IOException, SQLException {
        String ruling = "", tips = "", trivia = "";

        String cardLink = cardLinkTable.get(cardName)[0];
        String cardUrl = "http://yugioh.wikia.com" + cardLink;

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
        CardParser parser = new CardParser(cardName, mainDom);
        Card card = parser.parse();

        String ocgOnly = "0", tcgOnly = "0";
        if (tcgCards.contains(cardName) && !ocgCards.contains(cardName)) {
            tcgOnly = "1";
        }

        if (ocgCards.contains(cardName) && !tcgCards.contains(cardName)) {
            ocgOnly = "1";
        }

        String[] params = new String[] { cardName, card.getRealName(), card.getAttribute(), card.getCardType(),
                card.getTypes(), card.getLevel(), card.getAtk(), card.getDef(), card.getPasscode(),
                card.getEffectTypes(), card.getMaterials(), card.getFusionMaterials(), card.getRank(), card.getRitualSpell(),
                card.getPendulumScale(), card.getLinkMarkers(), card.getLink(), card.getProperty(), card.getSummonedBy(),
                card.getLimitText(), card.getSynchroMaterial(), card.getRitualMonster(), ruling, tips, trivia,
                card.getLore(), card.getArchetype(), card.getOcgStatus(), card.getTcgAdvStatus(), card.getTcgTrnStatus(),
                ocgOnly, tcgOnly, card.getImg(), cardUrl };

        for (int i = 0; i < params.length; i++) {
            psParms.setString(i+1, params[i]);
        }

        psParms.executeUpdate();
        doneCounter.incrementAndGet();
    }

    private static String getCardInfoGeneric(Document dom, boolean isTipsPage) {
        Element content = dom.getElementById("mw-content-text");
        return Util.getCleanedHtml(content, isTipsPage);
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

    //for possible future launch parameters
    private static void parseArgs(String[] args) {
        List<String> argsList = Arrays.asList(args);
        if (argsList.contains("--raw")) {
            boolean rawText = Boolean.parseBoolean(argsList.get(argsList.indexOf("--raw") + 1));
            Util.setRawText(rawText);
        }
    }

    private static void outputArgs(){
        logLine("Launched with settings:");
        String confirmRaw = "Raw Text: " + Boolean.toString(Util.getRawTextSetting());
        logLine(confirmRaw);
    }

    interface Work {
        void processItem(String itemName, AtomicInteger doneCounter) throws IOException, SQLException;
    }
}
