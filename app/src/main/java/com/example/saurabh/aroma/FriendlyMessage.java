package com.example.saurabh.aroma;

import java.util.Map;

public class FriendlyMessage {

    private String id;
    private String messages;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String type;
    String from;
    private long time;
    private boolean seen;

    public FriendlyMessage() {

    }

    public FriendlyMessage(String messages, String login_userName, String login_thumbImage, String loadingImageUrl, String text, Map<String, String> timestamp, boolean seen) {
    }

    public FriendlyMessage(String messages, String name, String photoUrl, String imageUrl, String type, long time, boolean seen) {
        this.messages = messages;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
