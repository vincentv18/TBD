package com.example.vincent.tbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private ArrayList<String> devices = new ArrayList<>();;
    private Map<String, NsdService> services = new HashMap<String, NsdService>();

    Network mNetwork;
    NsdHelper mNsdHelper;

    public static final String TAG = "NsdChat";

    // Creates data for list
    int i = 0;
    private void initializeData() {
        ItemList = new ArrayList<>();
        for(; i < 3; i++) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ItemList.add(new Item("Test "+ i, bm, "NULL"));
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
                String tst = msg.getData().getString("msg");
                receiveMsg(tst);
            }
        };
        mNetwork = new Network(mUpdateHandler);

        // Init NSD
        mNsdHelper = new NsdHelper(this, this);
        mNsdHelper.setLogEnabled(true);
        mNsdHelper.setAutoResolveEnabled(false);
        mNsdHelper.setDiscoveryTimeout(360);
        
        // Register device
        if(mNetwork.getLocalPort() > -1) {
            mNsdHelper.registerService(getLocalBluetoothName(), NsdType.HTTP, mNetwork.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
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
                // Starts Connection Activity
                // to see which device is available
                Intent connect = new Intent(this, ConnectionActivity.class);
                connect.putStringArrayListExtra("list", devices);
                startActivityForResult(connect, 1);
                return true;
            case R.id.pause_black:
                // Stop the animation and return to standby
                mGifImageView.setImageResource(R.drawable.black);
                sendMsg();
                return true;
            case R.id.playlist_black:
                // Starts the File Manager
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                filePicker.putExtra(FilePickerActivity.ARG_FILE_FILTER, Pattern.compile(".*(gif|mp4)$"));
                filePicker.putExtra(FilePickerActivity.ARG_DIRECTORIES_FILTER, false);
                filePicker.putExtra(FilePickerActivity.ARG_SHOW_HIDDEN, false);
                startActivityForResult(filePicker, 2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //--------------------------------------------------------------------------------------
    // Connection
    public void sendMsg() {
        mNetwork.sendMessage("Test");
    }

    public void receiveMsg(String line) {
        Toast.makeText(this, line, Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------------------------------------------------------
    // Results from activites
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Results from Connection Activity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String deviceName = data.getStringExtra("deviceName");
            if(services.containsKey(deviceName)) {
                mNsdHelper.resolveService(services.get(deviceName));
            } else {
                Toast.makeText(this, "Connection not established", Toast.LENGTH_SHORT).show();
            }
        }

        // Results from File Manager
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Returns the filepath from file manager
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            // Get thumbnail from gif
            Bitmap bm = null;
            try {
                GifDrawable gifFromPath = new GifDrawable(filePath);
                bm = Bitmap.createScaledBitmap(gifFromPath.getCurrentFrame(), 200, 200, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Strips path to just the filename
            String[] parts1 = filePath.split("/");
            String last = parts1[parts1.length-1];
            String[] parts2 = last.split("\\.");
            String fileName = parts2[0];
            // Adds the item to the RecyclerView
            ItemList.add(new Item(fileName, bm, filePath));
            mAdapter.notifyItemInserted(ItemList.size()-1);
        }
    }
    //--------------------------------------------------------------------------------------
    // NSD Wrappers
    @Override
    public void onNsdRegistered(NsdService registeredService) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceFound(NsdService foundService) {
        boolean found = false;
        String name = foundService.getName();
        // Doesn't add duplicates
        for (String device : devices) {
            if (device.equals(name)) { found = true; }
        }
        if (!found) {
            devices.add(foundService.getName());
            services.put(name, foundService);
        }
    }

    @Override
    public void onNsdServiceResolved(NsdService resolvedService) {
        // Establish connection with 2nd device
        mNetwork.connectToServer(resolvedService.getHost(),
                resolvedService.getPort());
    }

    @Override
    public void onNsdServiceLost(NsdService lostService) {
        String name = lostService.getName();
        // Removes service from list
        if (devices.contains(name)) {
            devices.remove(name);
            services.remove(name);
        }
    }

    @Override
    public void onNsdError(String errorMessage, int errorCode, String errorSource) {

    }

    //--------------------------------------------------------------------------------------
    // Activity states
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNsdHelper.stopDiscovery();
        mNsdHelper.unregisterService();
    }
}

