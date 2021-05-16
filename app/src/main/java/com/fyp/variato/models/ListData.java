package com.fyp.variato.models;

public class ListData {
    String bookingKey;

    private String currentDate;
    private String currentTime;
    private String name;
    int persons;
    int tableNo;
    String bookingDataAndTime;
    String BookedTimeSlotKey;
    int thisSlotReservedSeats;
    String bookingExpiryDate;
    String userUID;

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getBookingExpiryDate() {
        return bookingExpiryDate;
    }

    public void setBookingExpiryDate(String bookingExpiryDate) {
        this.bookingExpiryDate = bookingExpiryDate;
    }

    public int getThisSlotReservedSeats() {
        return thisSlotReservedSeats;
    }

    public void setThisSlotReservedSeats(int thisSlotReservedSeats) {
        this.thisSlotReservedSeats = thisSlotReservedSeats;
    }

    public String getBookingDataAndTime() {
        return bookingDataAndTime;
    }

    public String getBookedTimeSlotKey() {
        return BookedTimeSlotKey;
    }

    public void setBookedTimeSlotKey(String bookedTimeSlotKey) {
        BookedTimeSlotKey = bookedTimeSlotKey;
    }

    public void setBookingDataAndTime(String bookingDataAndTime) {
        this.bookingDataAndTime = bookingDataAndTime;
    }

    public int getTableNo() {
        return tableNo;
    }

    public void setTableNo(int tableNo) {
        this.tableNo = tableNo;
    }

    public String getBookingKey() {
        return bookingKey;
    }

    public void setBookingKey(String bookingKey) {
        this.bookingKey = bookingKey;
    }


    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPersons() {
        return persons;
    }

    public void setPersons(int persons) {
        this.persons = persons;
    }

    public ListData() {
    }

    public ListData(String name, String movie, String Date) {

        this.currentDate = name;
        this.currentTime = movie;
        this.name=Date;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getName() {
        return name;
    }
}
