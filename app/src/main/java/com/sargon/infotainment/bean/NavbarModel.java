package com.sargon.infotainment.bean;

public class NavbarModel {

    private String hour; //CONTROL
    private int battInt;
    private String batt;
    private Boolean bluetooth;
    private Boolean wifi;
    private int signal;


    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getBattInt() {
        return battInt;
    }

    public void setBattInt(int battInt) {
        this.battInt = battInt;
        setBatt(Integer.toString(battInt));
    }

    public String getBatt() {
        return batt;
    }

    public void setBatt(String batt) {
        this.batt = batt;
    }

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }
}
