package com.fyp.variato.models;

public class BookedTableModel {

    String name;
    String uuid;
    int noOfGuest;
    int tableNo;
    String tableUid;


    public BookedTableModel(String name, String uuid, int noOfGuest, int tableNo, String tableUid) {
        this.name = name;
        this.uuid = uuid;
        this.noOfGuest = noOfGuest;
        this.tableNo = tableNo;
        this.tableUid = tableUid;
    }
}
