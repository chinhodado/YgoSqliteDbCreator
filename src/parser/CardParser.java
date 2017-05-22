package parser;

import entity.Card;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Chin on 13-May-17.
 */
public class CardParser {
    private Element dom;
    private String cardName;

    public CardParser(String cardName, Document dom) {
        this.cardName = cardName;
        Element elem = dom.getElementById("mw-content-text");
        Util.removeSupTag(elem);
        this.dom = elem;
    }

    public Card parse() {
        String realName = "", attribute = "", cardType = "", types = "", level = "", atk = "", def = "", passcode = "",
                effectTypes = "", materials = "", fusionMaterials = "", rank = "", ritualSpell = "",
                pendulumScale = "", linkMarkers = "", link = "", property = "", summonedBy = "", limitText = "",
                synchroMaterial = "", ritualMonster = "", lore = "", archetype = "",
                ocgStatus = "", tcgAdvStatus = "", tcgTrnStatus = "", img = "";

        Element cardTable = dom.getElementsByClass("cardtable").first();
        Elements rows = cardTable.getElementsByClass("cardtablerow");

        try {
            Element imgAnchor = dom.getElementsByClass("cardtable-cardimage").first().getElementsByClass("image-thumbnail").first();
            String imgUrl = imgAnchor.attr("href");
            img = Util.getShortenedImageLink(imgUrl);
        }
        catch (Exception e) {
            /* do nothing */
        }

        if (!rows.first().getElementsByClass("cardtablerowheader").first().text().equals("English")) {
            Util.logLine("First row in table for " + cardName + " is not English name!");
        }

        String inPageName = rows.first().getElementsByClass("cardtablerowdata").first().text();
        if (!cardName.equals(inPageName)) {
            realName = inPageName;
        }

        // first row is "Card type". Be careful with this as it may change!
        boolean foundFirstRow = false;
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Element header = row.getElementsByClass("cardtablerowheader").first();
            if (header == null) continue;
            String headerText = header.text();
            if (!foundFirstRow && !headerText.equals("Card type")) {
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
                    case "Passcode"                     : passcode        = data; break; // 64163367
                    case "Card effect types"            : effectTypes     = data; break; // Continuous-like, Trigger-like
                    case "Materials"                    : materials       = data; break; // "Genex Controller" + 1 or more non-Tuner WATER monsters
                    case "Fusion Material"              : fusionMaterials = data; break; // "Blue-Eyes White Dragon"
                    case "Rank"                         : rank            = data; break; // 4
                    case "Ritual Spell Card required"   : ritualSpell     = data; break; // "Zera Ritual"
                    case "Pendulum Scale"               : pendulumScale   = data; break; // 1
                    case "Link Markers"                 : linkMarkers     = data; break; // Bottom-Left, Bottom-Right
                    case "Property"                     : property        = data; break; // Continuous
                    case "Summoned by the effect of"    : summonedBy      = data; break; // "Gorz the Emissary of Darkness"
                    case "Limitation text"              : limitText       = data; break; // This card cannot be in a Deck.
                    case "Synchro Material"             : synchroMaterial = data; break; // "Genex Controller"
                    case "Ritual Monster required"      : ritualMonster   = data; break; // "Zera the Mant"
                    case "Statuses":                                                     // Legal
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

        lore = getCardLore(dom);
        archetype = getArchetype(dom);

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
        card.setArchetype(archetype);
        card.setOcgStatus(ocgStatus);
        card.setTcgAdvStatus(tcgAdvStatus);
        card.setTcgTrnStatus(tcgTrnStatus);
        card.setImg(img);

        return card;
    }

    private String getCardLore(Element dom) {
        Element effectBox = dom.getElementsByClass("cardtablespanrow").first().getElementsByClass("navbox-list").first();
        String effect = Util.getCleanedHtml(effectBox, false, false);

        // turn <dl> into <p> and <dt> into <b>
        effect = effect.replace("<dl", "<p").replace("dl>", "p>").replace("<dt", "<b").replace("dt>", "b>");
        return effect;
    }

    private String getArchetype(Element dom){
        Element cardtableCategories = dom.getElementsByClass("cardtable-categories").first();
        if (cardtableCategories == null) return "";

        Set<String> tempset = new HashSet<>();

        for (Element hlist : cardtableCategories.getElementsByClass("hlist")) {
            Element dl = hlist.getElementsByTag("dl").first();
            String dt = dl.getElementsByTag("dt").first().text();
            Elements archetypes;

            //check if the left side contains the words archetypes
            if (!dt.toLowerCase().contains("archetypes")) continue;

            archetypes = dl.getElementsByTag("dd");

            //for multiple archetypes
            for (Element dd : archetypes) {
                String a = dd.getElementsByAttribute("href").first().text();
                tempset.add(a);
            }
        }

        String archetype = String.join(" , ", tempset);

        return archetype;
    }
}
