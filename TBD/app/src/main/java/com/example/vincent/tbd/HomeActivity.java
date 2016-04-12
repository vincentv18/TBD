package com.example.vincent.tbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private GifImageView mGifImageView;
    private List<Item> ItemList;

    // Creates data for list
    int i = 0;
    private void initializeData() {
        ItemList = new ArrayList<>();
        for(; i < 3; i++) {
            ItemList.add(new Item("Test "+ i, R.mipmap.ic_launcher, "NULL"));
        }
    }

    // Creates the adapter for items
    private void initializeAdapter() {
        mAdapter = new ItemAdapter(this, ItemList);
        mRecyclerView.setAdapter(mAdapter);
    }

    // Initialize recycler view
    private void initializeRecycler() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mGifImageView = (GifImageView) findViewById(R.id.gifView);
        initializeData();
        initializeRecycler();
        initializeAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        setTitle("Home");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection from menu
        switch (item.getItemId()) {
            case R.id.cast_black:
                // Connect the device to a host
                return true;
            case R.id.pause_black:
                // Stop the animation and return to standby
                stopAnimation();
                return true;
            case R.id.playlist_black:
                // Adds to RecyclerView
                addToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Stops currently playing animation
    private void stopAnimation() {
        mGifImageView.setImageResource(R.drawable.black);
    }

    // Adds items to list
    private void addToList() {
        // Starts the file manager
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.ARG_FILE_FILTER, Pattern.compile(".*(gif|mp4)$"));
        intent.putExtra(FilePickerActivity.ARG_DIRECTORIES_FILTER, false);
        intent.putExtra(FilePickerActivity.ARG_SHOW_HIDDEN, false);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Returns the filepath from file manager
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            // Strips path to just the filename
            String[] parts1 = filePath.split("/");
            String last = parts1[parts1.length-1];
            String[] parts2 = last.split("\\.");
            String fileName = parts2[0];
            // Adds the item to the list
            ItemList.add(new Item(fileName, R.mipmap.ic_launcher, filePath));
            mAdapter.notifyItemInserted(ItemList.size()-1);
        }
    }
}

