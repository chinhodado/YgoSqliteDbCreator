package com.chin.ygowikitool.entity;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Chin on 13-May-17.
 */
public class Card {
    private String realName = "", attribute = "", cardType = "", types = "", level = "", atk = "", def = "", passcode = "",
            effectTypes = "", materials = "", fusionMaterials = "", rank = "", ritualSpell = "",
            pendulumScale = "", linkMarkers = "", link = "", property = "", summonedBy = "", limitText = "", synchroMaterial = "", ritualMonster = "",
            lore = "", archetype = "", ocgStatus = "", tcgAdvStatus = "", tcgTrnStatus = "",
            img = "";

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

    public String getArchetype() {
        return archetype;
    }

    public List<String> getArchetypes() {
        return Arrays.asList(getArchetype().split(" , "));
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
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
}
