package com.moms.app;

import android.graphics.Bitmap;

public class Item {
    private String name;
    private Bitmap image;

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
}
