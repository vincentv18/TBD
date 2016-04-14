package com.example.vincent.tbd;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.rafakob.nsdhelper.NsdHelper;
import com.rafakob.nsdhelper.NsdListener;
import com.rafakob.nsdhelper.NsdService;
import com.rafakob.nsdhelper.NsdType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 4/13/16.
 */
public class ConnectionActivity extends AppCompatActivity implements NsdListener {

    private NsdHelper mNsdHelper;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private List<String> devices;
    private ArrayAdapter mAdapter;

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
        setTitle("Available Devices");

        // Init view
        setContentView(R.layout.activity_connection);
        mListView = (ListView) findViewById(R.id.device_list);

        // Init NSD
        mNsdHelper = new NsdHelper(this, this);
        mNsdHelper.setLogEnabled(true);
        mNsdHelper.setAutoResolveEnabled(false);
        mNsdHelper.setDiscoveryTimeout(360);
        mNsdHelper.registerService(getLocalBluetoothName(), NsdType.HTTP);
        // Discover device
        mNsdHelper.startDiscovery(NsdType.HTTP);

        // Init list
        devices = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, R.layout.device, devices);
        mListView.setAdapter(mAdapter);
    }

    //--------------------------------------------------------------------------------------
    // Activity states
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
        boolean found = false;
        String name = foundService.getName();
        // Doesn't add duplicates
        for (String device : devices) {
            if (device.equals(name)) { found = true; }
        }
        if (!found) {
            devices.add(foundService.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onNsdServiceResolved(NsdService resolvedService) {

    }

    @Override
    public void onNsdServiceLost(NsdService lostService) {
        String name = lostService.getName();
        for (String device : devices) {
            if (device.equals(name)) { devices.remove(device); }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onNsdError(String errorMessage, int errorCode, String errorSource) {

    }
}
