package com.example.picster.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Feed implements Serializable {
    private String id, username, date, imageUri, content;
    private int likes;
    private Boolean isPublic;
    private List<Comment> comments;
    private Boolean reported;

    public Feed() {
    }

    public Feed(String id, String username, String date, String imageUri, String content, int likes, boolean isPublic, List<Comment> comments, Boolean reported) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.imageUri = imageUri;
        this.content = content;
        this.likes = likes;
        this.isPublic = isPublic;
        this.comments = comments;
        this.reported = reported;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Boolean getReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }
}