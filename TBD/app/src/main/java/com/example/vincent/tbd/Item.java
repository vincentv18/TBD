package com.example.vincent.tbd;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 4/4/16.
 */
public class Item {
    String title;
    int thumbnail;

    Item(String title, int thumbnail) {
        this.title = title;
        this.thumbnail = thumbnail;
    }
}
