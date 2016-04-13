package com.example.vincent.tbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Environment;
import android.os.Message;
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
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.rafakob.nsdhelper.NsdHelper;
import com.rafakob.nsdhelper.NsdListener;
import com.rafakob.nsdhelper.NsdService;
import com.rafakob.nsdhelper.NsdType;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class HomeActivity extends AppCompatActivity implements NsdListener {
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private GifImageView mGifImageView;
    private List<Item> ItemList;
    private Handler mUpdateHandler;
    private BluetoothAdapter mBluetoothAdapter;

    ChatConnection mConnection;
    NsdHelper mNsdHelper;

    public static final String TAG = "NsdChat";

    // Creates data for list
    int i = 0;
    private void initializeData() {
        ItemList = new ArrayList<>();
        for(; i < 3; i++) {
            ItemList.add(new Item("Test "+ i, R.mipmap.ic_launcher, "NULL"));
        }
    }

    // Returns bluetooth name
    public String getLocalBluetoothName(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if(name == null){
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }
    //--------------------------------------------------------------------------------------
    // Initialize
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeData();
        // Init GifImage View
        mGifImageView = (GifImageView) findViewById(R.id.gifView);
        // Init Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Init Adapter for items
        mAdapter = new ItemAdapter(this, ItemList);
        mRecyclerView.setAdapter(mAdapter);

        // Init Connection
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                receiveMsg(message);
            }
        };
        mConnection = new ChatConnection(mUpdateHandler);

        // Init NSD
        mNsdHelper = new NsdHelper(this, this);
        mNsdHelper.setLogEnabled(true);
        mNsdHelper.setDiscoveryTimeout(360);
        mNsdHelper.registerService(getLocalBluetoothName(), NsdType.HTTP);
        // Discover device
        mNsdHelper.startDiscovery(NsdType.HTTP);
    }
    //--------------------------------------------------------------------------------------
    // Menu options
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
                clickCast();
                return true;
            case R.id.pause_black:
                // Stop the animation and return to standby
                mGifImageView.setImageResource(R.drawable.black);
                sendMsg();
                return true;
            case R.id.playlist_black:
                // Starts the file manager
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.ARG_FILE_FILTER, Pattern.compile(".*(gif|mp4)$"));
                intent.putExtra(FilePickerActivity.ARG_DIRECTORIES_FILTER, false);
                intent.putExtra(FilePickerActivity.ARG_SHOW_HIDDEN, false);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //--------------------------------------------------------------------------------------
    // Connection
    public void clickCast() {

    }

    public void sendMsg() {
        mConnection.sendMessage("Test");
    }

    public void receiveMsg(String line) {
        Toast.makeText(this, line, Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------------------------------------------------------
    // Data from FilePickerActivity
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
            // Adds the item to the RecyclerView
            ItemList.add(new Item(fileName, R.mipmap.ic_launcher, filePath));
            mAdapter.notifyItemInserted(ItemList.size()-1);
        }
    }
    //--------------------------------------------------------------------------------------
    // Activity states
    @Override
    protected void onStop() {
        super.onStop();
        mNsdHelper.stopDiscovery();
        mNsdHelper.unregisterService();
    }

    @Override
    public void onNsdRegistered(NsdService registeredService) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceFound(NsdService foundService) {

    }

    @Override
    public void onNsdServiceResolved(NsdService resolvedService) {
        Log.d(TAG, "Connecting.");
        mConnection.connectToServer(resolvedService.getHost(),
                resolvedService.getPort());

    }

    @Override
    public void onNsdServiceLost(NsdService lostService) {

    }

    @Override
    public void onNsdError(String errorMessage, int errorCode, String errorSource) {

    }
}

