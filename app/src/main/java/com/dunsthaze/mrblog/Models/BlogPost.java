package com.dunsthaze.mrblog.Models;

import java.util.ArrayList;

public class BlogPost extends BlogPostId{
    public String user_id;
    public String imageurl;
    public String title;
    public String date;
    public String time;
    public String address;

    public BlogPost(){

    }

    public BlogPost(String user_id, String imageurl, String title, String address, String date, String time) {
        this.user_id = user_id;
        this.imageurl = imageurl;
        this.title = title;
        this.address = address;
        this.date = date;
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImage_url(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
