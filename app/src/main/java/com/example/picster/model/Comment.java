package com.example.picster.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private String username;
    private String comment;

    public Comment() {
    }

    public Comment(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
