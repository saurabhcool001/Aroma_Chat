package com.example.saurabh.aroma;

/**
 * Created by saurabh on 28-07-2017.
 */

public class Friends {

    public String name;
    public String date;
    public String thumb_image;

    public Friends() {
    }

    public Friends(String name, String date, String thumb_image) {
        this.name = name;
        this.date = date;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
