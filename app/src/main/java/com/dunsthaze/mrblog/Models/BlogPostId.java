package com.dunsthaze.mrblog.Models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class BlogPostId {

    @Exclude
    public String blogPostId;

    public <T extends  BlogPostId> T withId(@NonNull final String id){
        this.blogPostId = id;
        return (T) this;
    }
}
