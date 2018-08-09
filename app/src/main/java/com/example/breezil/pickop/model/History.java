package com.example.breezil.pickop.model;

public class History {

    private String historyId;

    private String time;

    public History(String historyId, String time) {
        this.historyId = historyId;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }
}
