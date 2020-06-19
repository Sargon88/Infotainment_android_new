package com.sargon.infotainment.bean;

import java.util.Date;
import java.util.List;

public class PhoneStatus {

    private String page;
    private String longitude;
    private String latitude;
    private String callerId;
    private String yturl;
    private String lastUsbStatus;
    private int  brightness;
    private List<CallHistoryBean> lastCalls;
    private List<ContactBean> starredContacts; //CONTROL
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

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
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

    public List<CallHistoryBean> getLastCalls() {
        return lastCalls;
    }

    public void setLastCalls(List<CallHistoryBean> lastCalls) {
        this.lastCalls = lastCalls;
    }

    public List<ContactBean> getStarredContacts() {
        return starredContacts;
    }

    public void setStarredContacts(List<ContactBean> starredContacts) {
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
        if(navbar == null){
            navbar = new NavbarModel();
        }
        return navbar;
    }

    public void setNavbar(NavbarModel navbar) {
        this.navbar = navbar;
    }
}
