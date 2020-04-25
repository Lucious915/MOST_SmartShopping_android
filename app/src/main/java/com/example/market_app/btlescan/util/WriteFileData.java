package com.example.market_app.btlescan.util;

public class WriteFileData {
    public String TimeStamp;
    public int Minor;
    public int Major;
    public int RSSI;

    public void setTimeStamp(String T){
        TimeStamp = T;
    }

    public String getTimeStamp(){
        return TimeStamp;
    }

    public void setMinor(int M){
        Minor = M;
    }

    public int getMinor(){
        return Minor;
    }

    public void setRSSI(int R){
        RSSI = R;
    }

    public int getRSSI(){
        return RSSI;
    }

    public int getMajor() {
        return Major;
    }

    public void setMajor(int major) {
        Major = major;
    }
}

