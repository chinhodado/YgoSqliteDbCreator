package com.chin.ygowikitool.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chin on 13-May-17.
 */
public class Card {
    // name is the title of the article, which is guaranteed to be unique, but may not be the card's true name
    // e.g. it may be missing #, have "(card)" at the end, etc.
    private String name = "";

    // realName is the real name of the card, but may not be unique (e.g. tokens, egyptian gods, etc.)
    private String realName = "";
    private String attribute = "";
    private String cardType = "";
    private String types = "";
    private String level = "";
    private String atk = "";
    private String def = "";
    private String passcode = "";
    private String effectTypes = "";
    private String materials = "";
    private String fusionMaterials = "";
    private String rank = "";
    private String ritualSpell = "";
    private String pendulumScale = "";
    private String linkMarkers = "";
    private String link = "";
    private String property = "";
    private String summonedBy = "";
    private String limitText = "";
    private String synchroMaterial = "";
    private String ritualMonster = "";
    private String lore = "";
    private String ocgStatus = "";
    private String tcgAdvStatus = "";
    private String tcgTrnStatus = "";
    private String img = "";
    private String fullImgLink = "";

    private List<String> archetypes = new ArrayList<>();

    // these comes from the booster card table
    private String setNumber = "";
    private String rarity = "";
    private String category = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAtk() {
        return atk;
    }

    public void setAtk(String atk) {
        this.atk = atk;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getEffectTypes() {
        return effectTypes;
    }

    public void setEffectTypes(String effectTypes) {
        this.effectTypes = effectTypes;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public String getFusionMaterials() {
        return fusionMaterials;
    }

    public void setFusionMaterials(String fusionMaterials) {
        this.fusionMaterials = fusionMaterials;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRitualSpell() {
        return ritualSpell;
    }

    public void setRitualSpell(String ritualSpell) {
        this.ritualSpell = ritualSpell;
    }

    public String getPendulumScale() {
        return pendulumScale;
    }

    public void setPendulumScale(String pendulumScale) {
        this.pendulumScale = pendulumScale;
    }

    public String getLinkMarkers() {
        return linkMarkers;
    }

    public void setLinkMarkers(String linkMarkers) {
        this.linkMarkers = linkMarkers;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getSummonedBy() {
        return summonedBy;
    }

    public void setSummonedBy(String summonedBy) {
        this.summonedBy = summonedBy;
    }

    public String getLimitText() {
        return limitText;
    }

    public void setLimitText(String limitText) {
        this.limitText = limitText;
    }

    public String getSynchroMaterial() {
        return synchroMaterial;
    }

    public void setSynchroMaterial(String synchroMaterial) {
        this.synchroMaterial = synchroMaterial;
    }

    public String getRitualMonster() {
        return ritualMonster;
    }

    public void setRitualMonster(String ritualMonster) {
        this.ritualMonster = ritualMonster;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public List<String>getArchetypes() {
        return archetypes;
    }

    public void setArchetypes(List<String> archetypes) {
        this.archetypes = archetypes;
    }

    public String getArchetypeString() {
        StringBuilder s = new StringBuilder();
        for (String a : archetypes) {
            s.append(s.length() == 0 ? "" : ", ").append(a);
        }

        return s.toString();
    }

    public String getOcgStatus() {
        return ocgStatus;
    }

    public void setOcgStatus(String ocgStatus) {
        this.ocgStatus = ocgStatus;
    }

    public String getTcgAdvStatus() {
        return tcgAdvStatus;
    }

    public void setTcgAdvStatus(String tcgAdvStatus) {
        this.tcgAdvStatus = tcgAdvStatus;
    }

    public String getTcgTrnStatus() {
        return tcgTrnStatus;
    }

    public void setTcgTrnStatus(String tcgTrnStatus) {
        this.tcgTrnStatus = tcgTrnStatus;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getFullImgLink() {
        return fullImgLink;
    }

    public void setFullImgLink(String fullImgLink) {
        this.fullImgLink = fullImgLink;
    }

    public String getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(String setNumber) {
        this.setNumber = setNumber;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        String tmp = "<b>" + getDisplayName() + "</b><br>";
        if (!category.equals("")) {
            tmp += small(category) + "<br>";
        }
        if (cardType.equals("Spell") || cardType.equals("Trap")) {
            tmp += small(property + " " + cardType);
        }
        else {
            tmp += "<small>";
            if (!level.equals("")) {
                tmp += "Level " + level;
                if (!pendulumScale.equals("")) {
                    tmp += ", Pendulum Scale " + pendulumScale;
                }
                tmp += "<br>";
            }
            else if (!rank.equals("")) {
                tmp += "Rank " + rank + "<br>";
            }
            tmp += (attribute + " " + types + "<br>" + atk + "/" + (!link.equals("")? "LINK " + link : def) + "</small>");
        }

        if (!setNumber.equals("")) {
            tmp += "<br>" + small(setNumber);
            if (!rarity.equals("")) {
                tmp += small(" (" + rarity + ")");
            }
        }

        return small(tmp);
    }

    private String small(String text) {
        return "<small>" + text + "</small>";
    }

    private String smaller(String text) {
        return "<small>" + small(text) + "</small>";
    }

    /**
     * Get the display name for the card. Most of the time this is the card's "name" property,
     * which is its article name on wikia. However when this name is different from the card's
     * real name, we may want to use the real name instead. Note that we don't always want to
     * display the real name because the article name can be useful (e.g. all tokens's real name
     * is the same).
     * @return a name suitable to be displayed
     */
    public String getDisplayName() {
        String displayName = name;

        // for cards like Jinzo #7 where the # is always missing from the article name
        if (realName != null && realName.contains("#")) {
            displayName = realName;
        }

        // for cards with article name like "Pharaoh's Servant (card)"
        if (realName != null && !realName.equals("") && name.endsWith("(card)")) {
            displayName = realName;
        }

        return displayName;
    }
}
