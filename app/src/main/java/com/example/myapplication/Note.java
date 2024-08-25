package com.example.myapplication;


import com.google.firebase.Timestamp;

public class Note {
    String title;
    String content;
    Timestamp timestamp;
    //private String id;
//    public String getId() {
//        return id;
//    }
    public Note() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
