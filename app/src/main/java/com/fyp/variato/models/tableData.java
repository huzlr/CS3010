package com.fyp.variato.models;

public class tableData {

    private Boolean free;
    private String tableKey;
    private Long tableNo;
    private int guestNumbers;

    public int getGuestNumbers() {
        return guestNumbers;
    }

    public void setGuestNumbers(int guestNumbers) {
        this.guestNumbers = guestNumbers;
    }

    public tableData() {
    }

    public tableData(Boolean free, String tableKey, Long tableNo, int guestNumbers) {
        this.free = free;
        this.tableKey = tableKey;
        this.tableNo = tableNo;
        this.guestNumbers = guestNumbers;
    }

    public Boolean getFree() {
        return free;
    }

    public String getTableKey() {
        return tableKey;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public void setTableNo(Long tableNo) {
        this.tableNo = tableNo;
    }

    public Long getTableNo() {
        return tableNo;
    }
}
