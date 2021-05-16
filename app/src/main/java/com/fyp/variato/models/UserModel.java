package com.fyp.variato.models;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String username;
    private String userUID;
    private String userPhone;
    private String email;

    public UserModel() {

    }
    public UserModel(String username, String userUID, String userPhone, String email) {
        this.username = username;
        this.userUID = userUID;
        this.userPhone = userPhone;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
