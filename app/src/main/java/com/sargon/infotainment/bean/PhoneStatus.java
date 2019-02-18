package com.sargon.infotainment.bean;

import java.util.Date;
import java.util.List;

public class PhoneStatus {

    private String page;
    private Long longitude;
    private Long latitude;
    private String callerNum;
    private String yturl;
    private String lastUsbStatus;
    private int  brightness;
    private List<CallModel> lastCalls;
    private List<CallModel> starredContacts; //CONTROL
    private Date lastUpdate;
    private Boolean calling;
    private Boolean inCall;

    private NavbarModel navbar;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public String getCallerNum() {
        return callerNum;
    }

    public void setCallerNum(String callerNum) {
        this.callerNum = callerNum;
    }

    public String getYturl() {
        return yturl;
    }

    public void setYturl(String yturl) {
        this.yturl = yturl;
    }

    public String getLastUsbStatus() {
        return lastUsbStatus;
    }

    public void setLastUsbStatus(String lastUsbStatus) {
        this.lastUsbStatus = lastUsbStatus;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public List<CallModel> getLastCalls() {
        return lastCalls;
    }

    public void setLastCalls(List<CallModel> lastCalls) {
        this.lastCalls = lastCalls;
    }

    public List<CallModel> getStarredContacts() {
        return starredContacts;
    }

    public void setStarredContacts(List<CallModel> starredContacts) {
        this.starredContacts = starredContacts;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Boolean getCalling() {
        return calling;
    }

    public void setCalling(Boolean calling) {
        this.calling = calling;
    }

    public Boolean getInCall() {
        return inCall;
    }

    public void setInCall(Boolean inCall) {
        this.inCall = inCall;
    }

    public NavbarModel getNavbar() {
        return navbar;
    }

    public void setNavbar(NavbarModel navbar) {
        this.navbar = navbar;
    }
}
