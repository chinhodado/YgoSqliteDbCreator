package com.chin.ygowikitool.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.ygowikitool.entity.Card;

public class YugipediaCardParser {
    private Element dom;
    private String cardName;

    public YugipediaCardParser(String cardName, Element dom) {
        this.cardName = cardName;
        Element elem = dom.getElementById("mw-content-text");
        Util.removeSupTag(elem);
        this.dom = elem;
    }

    public Card parse() {
        String realName = "", attribute = "", cardType = "", types = "", level = "", atk = "", def = "", passcode = "",
                effectTypes = "", materials = "", fusionMaterials = "", rank = "", ritualSpell = "",
                pendulumScale = "", linkMarkers = "", link = "", property = "", summonedBy = "", limitText = "",
                synchroMaterial = "", ritualMonster = "", lore = "",
                ocgStatus = "", tcgAdvStatus = "", tcgTrnStatus = "", img = "";

        Element cardTable = dom.getElementsByClass("innertable").first();
        Elements rows = cardTable.getElementsByTag("tr");

        try {
            Element imgElem = dom.getElementsByClass("cardtable-main_image-wrapper").first().getElementsByTag("img").first();
            String imgUrl = imgElem.attr("src");
            img = Util.getShortenedYugipediaImageLink(imgUrl);
        }
        catch (Exception e) {
            /* do nothing */
        }

//        if (!rows.first().getElementsByClass("cardtablerowheader").first().text().equals("English")) {
//            Util.logLine("First row in table for " + cardName + " is not English name!");
//        }

        String inPageName = dom.getElementsByClass("heading").first().text();
        if (!cardName.equals(inPageName)) {
            realName = inPageName;
        }

        // first row is "Card type". Be careful with this as it may change!
        boolean foundFirstRow = false;
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Element header = row.getElementsByTag("th").first();
            if (header == null) continue;
            String headerText = header.text();
            if (!foundFirstRow && !headerText.equals("Card type")) {
                // TODO probably don't need this check? Yugipedia table seems to always start with this row
                continue;
            }
//            if (headerText.equals("Other card information") || headerText.equals("External links")) {
//                // we have reached the end for some reasons, exit now
//                break;
//            }
//            else {
                foundFirstRow = true;
                String data = row.getElementsByTag("td").first().text().trim();
                switch (headerText) {
                    case "Attribute"                    : attribute       = data; break; // EARTH
                    case "Card type"                    : cardType        = data; break; // Spell, Monster
                    case "Types"                        : types           = data; break; // Token, Fairy / Effect
                    case "Type"                         : types           = data; break; // Fiend
                    case "Level"                        : level           = data; break; // 6
                    case "ATK / DEF"                    : {                              // 2500 / 2000
                        atk = data.split(" / ")[0];
                        def = data.split(" / ")[1];
                        break;
                    }
                    case "ATK / LINK"                    : {                              // 1400 / 2
                        atk = data.split(" / ")[0];
                        link = data.split(" / ")[1];
                        break;
                    }
                    case "Password"                     : passcode        = data; break; // 64163367
                    case "Effect types"                 : effectTypes     = data; break; // Continuous-like, Trigger-like
                    case "Materials"                    : materials       = data; break; // "Genex Controller" + 1 or more non-Tuner WATER monsters
                    case "Fusion Material"              : fusionMaterials = data; break; // "Blue-Eyes White Dragon"
                    case "Rank"                         : rank            = data; break; // 4
                    case "Ritual required"              : ritualSpell     = data; break; // "Zera Ritual"
                    case "Pendulum Scale"               : pendulumScale   = data; break; // 1
                    case "Link Arrows"                  : {                              // Top , Bottom-Left , Bottom-Right
                        linkMarkers = data.replaceAll(" , ", ", ");
                        StringBuilder arrows = new StringBuilder();
                        for (String token : linkMarkers.split(", ")) {
                            // remove nbsp
                            String s = '|' + token.replace("\u00a0", "").trim() + '|';
                            arrows.append(arrows.length() == 0 ? "" : ", ").append(s);
                        }
                        linkMarkers = arrows.toString();
                        break;
                    }
                    case "Property"                     : property        = data; break; // Continuous
                    case "Summoned by the effect of"    : summonedBy      = data; break; // "Gorz the Emissary of Darkness"
                    case "Limitation text"              : limitText       = data; break; // This card cannot be in a Deck.
                    case "Synchro Material"             : synchroMaterial = data; break; // "Genex Controller"
                    case "Ritual Monster required"      : ritualMonster   = data; break; // "Zera the Mant"
                    case "Status":                                                       // Legal
                        try {
                            String[] tokens = data.split("\\)");
                            for (String token : tokens) {
                                String[] tokens2 = token.split("\\(");
                                String status = tokens2[0].trim();
                                if (status.equals("Unlimited")) {
                                    status = "U";
                                }

                                String format = tokens2[1];
                                if (format.contains("Speed Duel")) {
                                    continue;
                                }
                                else if (format.contains("OCG")) {
                                    ocgStatus = status;
                                }
                                else if (format.contains("Advanced")) {
                                    tcgAdvStatus = status;
                                }
                                else if (format.contains("Traditional")) {
                                    tcgTrnStatus = status;
                                }
                                else if (format.contains("TCG")) {
                                    tcgAdvStatus = status;
                                    tcgTrnStatus = status;
                                }
                            }
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

                // rely on the assumption that Status is always the last info row
                if (headerText.equals("Status")) {
                    break;
                }
//            }
        }

        lore = getCardLore(dom);
        List<String> archetypes = getArchetypes(dom);

        Card card = new Card();
        card.setRealName(realName);
        card.setAttribute(attribute);
        card.setCardType(cardType);
        card.setTypes(types);
        card.setLevel(level);
        card.setAtk(atk);
        card.setDef(def);
        card.setPasscode(passcode);
        card.setEffectTypes(effectTypes);
        card.setMaterials(materials);
        card.setFusionMaterials(fusionMaterials);
        card.setRank(rank);
        card.setRitualSpell(ritualSpell);
        card.setPendulumScale(pendulumScale);
        card.setLinkMarkers(linkMarkers);
        card.setLink(link);
        card.setProperty(property);
        card.setSummonedBy(summonedBy);
        card.setLimitText(limitText);
        card.setSynchroMaterial(synchroMaterial);
        card.setRitualMonster(ritualMonster);
        card.setLore(lore);
        card.setOcgStatus(ocgStatus);
        card.setTcgAdvStatus(tcgAdvStatus);
        card.setTcgTrnStatus(tcgTrnStatus);
        card.setImg(img);

        card.setArchetypes(archetypes);

        return card;
    }

    private String getCardLore(Element dom) {
        Element effectBox = dom.getElementsByClass("lore").first();
        String effect = Util.getCleanedHtml(effectBox, false, false);

        // turn <dl> into <p> and <dt> into <b>
        effect = effect.replace("<dl", "<p").replace("dl>", "p>").replace("<dt", "<b").replace("dt>", "b>");

        if (effect.startsWith("<p>") && effect.endsWith("</p>")) {
            effect = effect.substring("<p>".length(), effect.length() - "</p>".length()).trim();
        }
        return effect;
    }

    private List<String> getArchetypes(Element dom){
        try {
            Elements elements = dom.getElementsByClass("mw-parser-output").first().children();

            List<String> archetypes = new ArrayList<>();

            boolean foundHeader = false;
            for (Element element : elements) {
                if (element.tagName().equals("h2") && element.text().trim().equals("Search categories")) {
                    foundHeader = true;
                    continue;
                }

                if (foundHeader) {
                    if (element.tagName().equals("div") && element.className().equals("hlist")) {
                        Element dl = element.getElementsByTag("dl").first();
                        String dt = dl.getElementsByTag("dt").first().text();

                        //check if the left side starts with the words Archetypes
                        if (!dt.startsWith("Archetypes and series")) continue;

                        Elements dds = dl.getElementsByTag("dd");

                        //for multiple archetypes
                        for (Element dd : dds) {
                            String txt = dd.getElementsByTag("a").first().text();
                            archetypes.add(txt);
                        }
                    }
                    else {
                        break;
                    }
                }
            }

            return archetypes;
        }
        catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
