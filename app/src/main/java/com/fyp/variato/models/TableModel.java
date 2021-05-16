package com.fyp.variato.models;

public class TableModel {

    String tableKey;
    int tableNo;
    Boolean isFree;
    private int guestNumbers;


    public Boolean getFree() {
        return isFree;
    }

    public void setFree(Boolean free) {
        isFree = free;
    }

    public TableModel(String tableKey, int tableNo, Boolean isFree, int guestNumbers) {
        this.tableKey = tableKey;
        this.tableNo = tableNo;
        this.isFree = isFree;
        this.guestNumbers = guestNumbers;
    }

    public TableModel(String tableKey, int tableNo) {
        this.tableKey = tableKey;
        this.tableNo = tableNo;
    }

    public String getTableKey() {
        return tableKey;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public int getTableNo() {
        return tableNo;
    }

    public void setTableNo(int tableNo) {
        this.tableNo = tableNo;
    }
}
