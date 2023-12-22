package com.example.picster.model;

public class User {
    private String email, username;
    private boolean VIP; // isPaid?

    public User(String email) {
        this.email = email;
        this.username = "";
        this.VIP = false;
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isVIP() {
        return VIP;
    }

    public void setVIP(boolean VIP) {
        this.VIP = VIP;
    }
}
