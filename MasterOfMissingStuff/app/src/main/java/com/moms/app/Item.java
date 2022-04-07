package com.moms.app;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Item {
    private String name;
    private Bitmap image;
    private boolean track;
    private boolean lock;
    private String idName;

    public Item(String itemName, Bitmap itemImage, boolean isTracked, boolean isLocked, String itemId) {
        name = itemName;
        image = itemImage;
        track = isTracked;
        lock = isLocked;
        idName = itemId;
    }

    public Item(String itemName, Bitmap itemImage) {
        name = itemName;
        image = itemImage;
    }

    public Item(String itemName) {
        name = itemName;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setLock(boolean isLocked) {
        lock = isLocked;
    }

    public void setTrack(boolean isTracked) {
        track = isTracked;
    }

    public boolean isLocked() { return lock; }

    public boolean isTracked() { return track; }

    public void setIdName(String itemId) { idName = itemId; }

    public String getIdName() { return idName; }
}
