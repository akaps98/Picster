package com.example.picster.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email, username;
    private boolean VIP; // isPaid?
    private List<User> friends;

    public User(String email) {
        this.email = email;
        this.username = "";
        this.VIP = false;
        this.friends = new ArrayList<>();
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

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
