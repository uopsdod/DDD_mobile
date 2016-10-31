package com.example.sam.drawerlayoutprac;

/**
 * Created by cuser on 2016/10/29.
 */

public class PartnerVO {
    Byte[] profile;
    String name;
    String introShort;
    String introLong;

    public PartnerVO(Byte[] profile, String name, String introShort, String introLong) {
        this.profile = profile;
        this.name = name;
        this.introShort = introShort;
        this.introLong = introLong;
    }

    public Byte[] getProfile() {
        return profile;
    }

    public void setProfile(Byte[] profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroShort() {
        return introShort;
    }

    public void setIntroShort(String introShort) {
        this.introShort = introShort;
    }

    public String getIntroLong() {
        return introLong;
    }

    public void setIntroLong(String introLong) {
        this.introLong = introLong;
    }
}
