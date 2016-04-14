package com.example.vincent.tbd;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 4/4/16.
 */
public class Item {
    String title, path;
    Bitmap thumbnail;

    Item(String title, Bitmap thumbnail, String path) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.path = path;
    }
}
