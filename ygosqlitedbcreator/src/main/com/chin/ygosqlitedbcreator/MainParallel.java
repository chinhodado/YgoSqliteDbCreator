package com.chin.ygosqlitedbcreator;

import static com.chin.ygowikitool.parser.Util.logLine;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.json.JSONException;

import com.chin.ygowikitool.api.YugiohApi;
import com.chin.ygowikitool.api.YugipediaApi;
import com.chin.ygowikitool.entity.Booster;
import com.chin.ygowikitool.entity.Card;

public class MainParallel {
    private static List<String> cardList;
    private static Set<String> ocgCards;
    private static Set<String> tcgCards;
    private static Map<String, String> cardLinkTable;

    private static Map<String, Set<Integer>> archetypeMap = new ConcurrentHashMap<>();
    private static final Object archetypeMapLock = new Object();

    private static List<String> boosterList;
    private static Set<String> ocgBoosters;
    private static Set<String> tcgBoosters;
    private static Map<String, String> boosterLinkTable;

    private static PreparedStatement psParms;
    private static PreparedStatement psBoosterInsert;
    private static PreparedStatement psArchetypeInsert;
    private static PreparedStatement psCardArchetypeInsert;
    private static final AtomicInteger cardDoneCounter = new AtomicInteger();
    private static final AtomicInteger boosterDoneCounter = new AtomicInteger();
    private static int iteration = 0;
    private static boolean rawText = false;
    private static final Object psCardInsertLock = new Object();

    private static final YugipediaApi yugipediaApi = new YugipediaApi();
    private static Map<String, String> yugipediaRulingMap;

    private static final YugiohApi YUGIOH_API = new YugipediaApi();

    // settings
    private static final boolean ENABLE_VERBOSE_LOG = true;
    private static final boolean ENABLE_TRIVIA = true;
    private static final boolean ENABLE_TIPS = true;
    private static final int NUM_THREAD = 8;

    public static void main (String[] args) throws InterruptedException, JSONException, IOException, ClassNotFoundException, SQLException {
        parseArgs(args);
        outputArgs();

        initializeCardList();
        initializeBoosterList();

        logLine("Fetching ruling list from Yugipedia");
        yugipediaRulingMap = yugipediaApi.getRulingMap();

        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        Statement stmt = connection.createStatement();
        String sql = "CREATE TABLE Card " +
                "( id                INTEGER PRIMARY KEY, " +
                "  name              TEXT NOT NULL, " +
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
                   "  ocgStatus   TEXT, "  +
                   "  tcgAdvStatus TEXT, " +
                   "  tcgTrnStatus TEXT, " +
                   "  ocgOnly   INTEGER, " +
                   "  tcgOnly   INTEGER, " +
                   "  img TEXT) ";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Archetype (id INTEGER PRIMARY KEY, name TEXT)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Card_Archetype (" +
                "cardId INTEGER, " +
                "archetypeId INTEGER, " +
                "FOREIGN KEY(cardId) REFERENCES Card(id), " +
                "FOREIGN KEY (archetypeId) REFERENCES Card_Archetype(id))";
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
        stmt.executeUpdate("CREATE INDEX ocgStatus_idx ON Card (ocgStatus)");
        stmt.executeUpdate("CREATE INDEX tcgAdvStatus_idx ON Card (tcgAdvStatus)");
        stmt.executeUpdate("CREATE INDEX tcgTrnStatus_idx ON Card (tcgTrnStatus)");
        stmt.executeUpdate("CREATE INDEX ocgOnly_idx ON Card (ocgOnly)");
        stmt.executeUpdate("CREATE INDEX tcgOnly_idx ON Card (tcgOnly)");
        stmt.executeUpdate("CREATE INDEX archetype_name_idx ON Archetype (name)");
        stmt.executeUpdate("CREATE INDEX card_archetype_cardId_idx ON Card_Archetype (cardId)");
        stmt.executeUpdate("CREATE INDEX card_archetype_archetypeId_idx ON Card_Archetype (archetypeId)");
        stmt.executeUpdate("CREATE INDEX booster_name_idx ON Booster (name)");

        psParms = connection.prepareStatement(
                "INSERT INTO Card (id, name, realName, attribute, cardType, types, level, atk, def, passcode, " +
                "effectTypes, materials, fusionMaterials, rank, ritualSpell, " +
                "pendulumScale, linkMarkers, link, property, summonedBy, limitText, synchroMaterial, ritualMonster, " +
                "ruling, tips, trivia, lore, ocgStatus, tcgAdvStatus, tcgTrnStatus, ocgOnly, tcgOnly, img) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        psBoosterInsert = connection.prepareStatement(
                "INSERT INTO Booster (name, enReleaseDate, jpReleaseDate, skReleaseDate, worldwideReleaseDate, imgSrc) " +
                        "VALUES (?,?,?,?,?,?)");

        psArchetypeInsert = connection.prepareStatement("INSERT INTO Archetype (id, name) VALUES (?,?)");
        psCardArchetypeInsert = connection.prepareStatement("INSERT INTO Card_Archetype(cardId, archetypeId) VALUES (?,?)");

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

        persistArchetypes(stmt);

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

    private static void persistArchetypes(Statement stmt) throws SQLException {
        int archetypeId = 0;
        for (String archetype : archetypeMap.keySet()) {
            psArchetypeInsert.setInt(1, archetypeId);
            psArchetypeInsert.setString(2, archetype);
            psArchetypeInsert.executeUpdate();

            Set<Integer> cardsInArchetype = archetypeMap.get(archetype);
            for (Integer cardId : cardsInArchetype) {
                psCardArchetypeInsert.setInt(1, cardId);
                psCardArchetypeInsert.setInt(2, archetypeId);
                psCardArchetypeInsert.executeUpdate();
            }
            archetypeId++;
        }
    }

    private static void initializeCardList() throws IOException, JSONException {
//        logLine("Fetching Wikia TCG card list");
//        YugiohWikiaApi yugiohWikiaApi = new YugiohWikiaApi();
//        Map<String, String> tcgCardMapWikia = yugiohWikiaApi.getCardMap(true);
//        Set<String> tcgCardsWikia = new HashSet<>(tcgCardMapWikia.keySet());
//
//        logLine("Fetching Wikia OCG card list");
//        Map<String, String> ocgCardMapWikia = yugiohWikiaApi.getCardMap(false);
//        Set<String> ocgCardsWikia = new HashSet<>(ocgCardMapWikia.keySet());
//
//        Map<String, String> cardLinkTableWikia = new HashMap<>(tcgCardMapWikia);
//        ocgCardMapWikia.forEach(cardLinkTableWikia::putIfAbsent);
//        List<String> cardListWikia = new ArrayList<>(cardLinkTableWikia.keySet());

        Map<String, String> tcgCardMap = YUGIOH_API.getCardMap(true);
        tcgCards = new HashSet<>(tcgCardMap.keySet());

        logLine("Fetching OCG card list");
        Map<String, String> ocgCardMap = YUGIOH_API.getCardMap(false);
        ocgCards = new HashSet<>(ocgCardMap.keySet());

        cardLinkTable = new HashMap<>(tcgCardMap);
        ocgCardMap.forEach(cardLinkTable::putIfAbsent);
        cardList = new ArrayList<>(cardLinkTable.keySet());

//        Set<String> set1 = new HashSet<>(cardList);
//        Set<String> setWikia = new HashSet<>(cardListWikia);
//        setWikia.removeAll(set1);
//        logLine("Cards in Wikia but not in Yugipedia: ");
//        System.out.println(setWikia);

        logLine("tcgCards: " + tcgCards.size() + ", ocgCards: " + ocgCards.size() + ", cardList: " + cardList.size());
//        logLine("tcgCardsWikia: " + tcgCardsWikia.size() + ", ocgCardsWikia: " + ocgCardsWikia.size() + ", cardListWikia: " + cardListWikia.size());
    }

    private static void initializeBoosterList() throws IOException, JSONException {
        logLine("Fetching TCG booster list");
        Map<String, String> tcgBoosterMap = YUGIOH_API.getBoosterMap(true);
        tcgBoosters = new HashSet<>(tcgBoosterMap.keySet());

        logLine("Fetching OCG booster list");
        Map<String, String> ocgBoosterMap = YUGIOH_API.getBoosterMap(false);
        ocgBoosters = new HashSet<>(ocgBoosterMap.keySet());

        boosterLinkTable = new HashMap<>(tcgBoosterMap);
        ocgBoosterMap.forEach(boosterLinkTable::putIfAbsent);
        boosterList = new ArrayList<>(boosterLinkTable.keySet());
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
            parts.add(new ArrayList<>(workList.subList(i, i + CHUNK_SIZE)));
        }

        parts.add(new ArrayList<>(workList.subList(size - LAST_CHUNK, size)));

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < numThread; i++) {
            List<String> part = parts.get(i);
            Runnable r = () -> {
                for (String card : part) {
                    try {
                        workFunction.processItem(card, doneCounter);
                    } catch (Exception e) {
                        System.out.println("Error with " + card);
                        e.printStackTrace();
                        errorList.add(card);
                    }
                }
            };
            Thread thread = new Thread(r);
            thread.start();
            threadList.add(thread);
        }

        Thread logThread = new Thread(() -> {
            JFrame frame = new JFrame();

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            frame.setLayout(new GridBagLayout());
            frame.setTitle("YgoSqliteDbCreator");
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.insets = new Insets(5, 5, 5, 5);

            frame.add(textArea, constraints);
            frame.setVisible(true);

            while (!Thread.currentThread().isInterrupted()) {
                int done = doneCounter.get();
                int error = errorList.size();
                double percentage = (double)done / totalWorkSize * 100;
                textArea.setText("Completed: " + done + "/" + totalWorkSize + "(" +
                        String.format("%.2f", percentage) + "%), error: " + error);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    frame.setVisible(false);
                    frame.dispose();
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
        String boosterLink = boosterLinkTable.get(boosterName);
        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + boosterName + "'s general info");
        Booster booster = YUGIOH_API.getBooster(boosterName, boosterLink);

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

        String cardLink = cardLinkTable.get(cardName);

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s ruling");
        ruling = YUGIOH_API.getRuling(cardLink);

        if (ENABLE_TIPS) {
            if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s tips");
            tips = YUGIOH_API.getTips(cardLink);
        }

        if (ENABLE_TRIVIA) {
            if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s trivia");
            trivia = YUGIOH_API.getTrivia(cardLink);
        }

        if (ENABLE_VERBOSE_LOG) System.out.println("Fetching " + cardName + "'s general info");
        Card card = YUGIOH_API.getCard(cardName, cardLink);

        String ocgOnly = "", tcgOnly = "";
        if (tcgCards.contains(cardName) && !ocgCards.contains(cardName)) {
            tcgOnly = "1";
        }

        if (ocgCards.contains(cardName) && !tcgCards.contains(cardName)) {
            ocgOnly = "1";
        }

        int id = doneCounter.incrementAndGet();

        String[] params = new String[] { cardName, card.getRealName(), card.getAttribute(), card.getCardType(),
                card.getTypes(), card.getLevel(), card.getAtk(), card.getDef(), card.getPasscode(),
                card.getEffectTypes(), card.getMaterials(), card.getFusionMaterials(), card.getRank(), card.getRitualSpell(),
                card.getPendulumScale(), card.getLinkMarkers(), card.getLink(), card.getProperty(), card.getSummonedBy(),
                card.getLimitText(), card.getSynchroMaterial(), card.getRitualMonster(), ruling, tips, trivia,
                card.getLore(), card.getOcgStatus(), card.getTcgAdvStatus(), card.getTcgTrnStatus(),
                ocgOnly, tcgOnly, card.getImg() };

        synchronized (psCardInsertLock) {
            psParms.setInt(1, id);
            for (int i = 0; i < params.length; i++) {
                psParms.setString(i + 2, params[i]);
            }

            psParms.executeUpdate();
        }

        synchronized (archetypeMapLock) {
            List<String> archetypes = card.getArchetypes();
            for (String archetype : archetypes) {
                if (archetype == null || "".equals(archetype)) continue;

                archetypeMap.putIfAbsent(archetype, new HashSet<>());
                Set<Integer> cardsInArchetype = archetypeMap.get(archetype);
                cardsInArchetype.add(id);
            }
        }
    }

    //for possible future launch parameters
    private static void parseArgs(String[] args) {
        List<String> argsList = Arrays.asList(args);
        if (argsList.contains("--raw")) {
            rawText = Boolean.parseBoolean(argsList.get(argsList.indexOf("--raw") + 1));
        }
    }

    private static void outputArgs(){
        logLine("Launched with settings:");
        String confirmRaw = "Raw Text: " + rawText;
        logLine(confirmRaw);
    }

    interface Work {
        void processItem(String itemName, AtomicInteger doneCounter) throws IOException, SQLException;
    }
}
