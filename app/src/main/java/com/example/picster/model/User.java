package com.example.picster.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email, username;
    private boolean VIP; // isPaid?
    private List<String> friends;
    private CreditCard creditCard;
    private List<String> like;
    private List<String> save;

    public User() {
        this.email = "";
        this.username = "";
        this.VIP = false;
        this.friends = new ArrayList<>();
        this.creditCard = new CreditCard();
        this.like = new ArrayList<>();
        this.save = new ArrayList<>();
    }

    public User(String email) {
        this.email = email;
        this.username = "";
        this.VIP = false;
        this.friends = new ArrayList<>();
        this.creditCard = new CreditCard();
        this.like = new ArrayList<>();
        this.save = new ArrayList<>();
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.VIP = false;
        this.friends = new ArrayList<>();
        this.creditCard = new CreditCard();
        this.like = new ArrayList<>();
        this.save = new ArrayList<>();
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

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public List<String> getLike() {
        return like;
    }

    public void setLike(List<String> like) {
        this.like = like;
    }

    public List<String> getSave() {
        return save;
    }

    public void setSave(List<String> save) {
        this.save = save;
    }
}
