package com.aei.androidsecuritymasterapp;

public class Device {
    private String uid;
    private String email;

    public Device() {
    }

    public Device(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public Device setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Device setEmail(String email) {
        this.email = email;
        return this;
    }
}
