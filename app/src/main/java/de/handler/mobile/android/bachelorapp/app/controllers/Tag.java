package de.handler.mobile.android.bachelorapp.app.controllers;

/**
 * Tag V/O
 */
public class Tag {

    /**
     * stores how often the tag is used in app
     */
    private int count;
    /**
     * stores the tag string
     */
    private String tag;

    public Tag(int count, String tag) {
        this.count = count;
        this.tag = tag;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTag() {
        return tag;
    }
}
