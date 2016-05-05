package com.example.vincent.tbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

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
    private ProgressDialog mProgressDialog;
    private Menu mMenu;

    private ArrayList<String> devices = new ArrayList<>();
    private Map<String, NsdService> services = new HashMap<String, NsdService>();

    Network mNetwork;
    NsdHelper mNsdHelper;

    Storage storage = SimpleStorage.getExternalStorage();

    public static final String TAG = "NsdChat";

    //--------------------------------------------------------------------------------------
    // Helper Functions
    private void initializeData() {
        // Gets data from external storage
        String content = storage.readTextFile("GifHub", "animation_list.txt");
        String[] info = content.split("\\r?\\n");
        if (!content.equals("")) {
            for(int i = 0; i < info.length; ++i) {
                String[] files = info[i].split(":");
                Bitmap bm = null;
                if (files[1].equals("_NONE_")){
                    try {
                        if (files[0].equals("Cleat Hitch")) {
                            GifDrawable gifOne = new GifDrawable(getResources(), R.drawable.cleat);
                            bm = Bitmap.createScaledBitmap(gifOne.getCurrentFrame(), 200, 200, true);
                        }
                        else if (files[0].equals("Sit Down")) {
                            GifDrawable gifTwo = new GifDrawable( getResources(), R.drawable.sit);
                            bm = Bitmap.createScaledBitmap(gifTwo.getCurrentFrame(), 200, 200, true);
                        }
                        else if (files[0].equals("Stop Engine")) {
                            GifDrawable gifThree = new GifDrawable( getResources(), R.drawable.engine);
                            bm = Bitmap.createScaledBitmap(gifThree.getCurrentFrame(), 200, 200, true);
                        }
                        ItemList.add(new Item(files[0], bm, files[1]));
                        mAdapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    bm = getThumbnail(files[1]);
                    ItemList.add(new Item(files[0], bm, files[1]));
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    // Creates the beginning gifs
    // Adds the starting gifs to the RecyclerView and storage
    private void createGif() throws IOException {
        //Cleat Hitch
        GifDrawable gifOne = new GifDrawable( getResources(), R.drawable.cleat);
        Bitmap bm1 = Bitmap.createScaledBitmap(gifOne.getCurrentFrame(), 200, 200, true);
        ItemList.add(new Item("Cleat Hitch", bm1, "_NONE_"));
        storage.appendFile("GifHub", "animation_list.txt", "Cleat Hitch" + ":" + "_NONE_" + ":");
        //Sit Down
        GifDrawable gifTwo = new GifDrawable( getResources(), R.drawable.sit);
        Bitmap bm2 = Bitmap.createScaledBitmap(gifTwo.getCurrentFrame(), 200, 200, true);
        ItemList.add(new Item("Sit Down", bm2, "_NONE_"));
        storage.appendFile("GifHub", "animation_list.txt", "Sit Down" + ":" + "_NONE_" + ":");
        //Stop Engine
        GifDrawable gifThree = new GifDrawable( getResources(), R.drawable.engine);
        Bitmap bm3 = Bitmap.createScaledBitmap(gifThree.getCurrentFrame(), 200, 200, true);
        ItemList.add(new Item("Stop Engine", bm3, "_NONE_"));
        storage.appendFile("GifHub", "animation_list.txt", "Stop Engine" + ":" + "_NONE_" + ":");

        mAdapter.notifyDataSetChanged();
    }

    // Get thumbnail from gif
    Bitmap getThumbnail(String filePath) {
        Bitmap bm = null;
        try {
            GifDrawable gifFromPath = new GifDrawable(filePath);
            bm = Bitmap.createScaledBitmap(gifFromPath.getCurrentFrame(), 200, 200, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
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

        // Init GifImage View
        mGifImageView = (GifImageView) findViewById(R.id.gifView);
        // Init Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Init Adapter for items
        ItemList = new ArrayList<>();
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

        // Storage
        storage.createDirectory("GifHub");
        if (!storage.isFileExist("GifHub", "animation_list.txt")) {
            storage.createFile("GifHub", "animation_list.txt", "");
            try {
                createGif();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            initializeData();
        }
    }
    //--------------------------------------------------------------------------------------
    // Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        setTitle("Hub");
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection from menu
        switch (item.getItemId()) {
            case R.id.cast_black:
                // Starts Connection Activity
                // to see which device is available
                cast();
                return true;
            case R.id.stop_black:
                // Stop the animation and return to standby
                mGifImageView.setImageResource(R.drawable.black);
                sendMsg("_STOP_ANIMATION_");
                return true;
            case R.id.playlist_black:
                // Starts the File Manager
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                filePicker.putExtra(FilePickerActivity.ARG_FILE_FILTER, Pattern.compile(".*(gif)$"));
                filePicker.putExtra(FilePickerActivity.ARG_START_PATH, "/sdcard/Download");
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
    public void cast() {
        if(mNetwork.isClientConnected()) {
            //AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle("Disconnect")
                    .setMessage("Are you sure you want to disconnect?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue to available lists
                            mNetwork.tearDownClient();
                            startConnectionActivity();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            startConnectionActivity();
        }
    }

    public void startConnectionActivity() {
        Intent connect = new Intent(this, ConnectionActivity.class);
        connect.putStringArrayListExtra("list", devices);
        startActivityForResult(connect, 1);
    }

    public void sendMsg(String line) {
        mNetwork.sendMessage(line);
    }

    public void receiveMsg(String line) {
        // Checks if client or local
        if(Character.toString(line.charAt(0)).equals("c")) {
            String fileName = line.substring(1);
            if (fileName.equals("_CAST_MODE_INITIALIZE_")) {
                // Start "Cast Mode"
                Intent cast = new Intent(this, CastActivity.class);
                startActivity(cast);
            }
            else if (fileName.equals("_STOP_ANIMATION_")){
                CastActivity.stop();
            }
            else {
                CastActivity.display(fileName, storage, this);
            }
        }
    }
    //--------------------------------------------------------------------------------------
    // Results from activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Results from Connection Activity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String deviceName = data.getStringExtra("deviceName");
            if(services.containsKey(deviceName)) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("Connecting...");
                mProgressDialog.show();
                mNsdHelper.resolveService(services.get(deviceName));
            } else {
                Toast.makeText(this, "Connection not established", Toast.LENGTH_SHORT).show();
            }
        }

        // Results from File Manager
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Returns the filepath from file manager
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            // Get thumbnail
            Bitmap bm = getThumbnail(filePath);

            // Strips path to just the filename
            String[] parts1 = filePath.split("/");
            String last = parts1[parts1.length-1];
            String[] parts2 = last.split("\\.");
            String fileName = parts2[0];

            // Adds the item to the RecyclerView and storage
            ItemList.add(new Item(fileName, bm, filePath));
            storage.appendFile("GifHub", "animation_list.txt", fileName + ":" + filePath + ":");
            mAdapter.notifyDataSetChanged();
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
        // Start "Cast mode"
        mUpdateHandler.postDelayed(r, 2500);
    }

    Runnable r = new Runnable() {
        @Override
        public void run(){
            sendMsg("_CAST_MODE_INITIALIZE_");
            mProgressDialog.dismiss();
            MenuItem cast = mMenu.findItem(R.id.cast_black);
            cast.setIcon(R.mipmap.ic_cast_white_24dp);
        }
    };

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
        if (mNetwork.isClientConnected()) {
            mNetwork.tearDown();
        }
        else {
            mNetwork.tearDownServer();
        }
    }

}

