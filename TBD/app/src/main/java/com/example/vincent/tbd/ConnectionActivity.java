package com.example.vincent.tbd;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 4/13/16.
 */
public class ConnectionActivity extends AppCompatActivity {

    private ListView mListView;
    private List<String> devices;
    private ArrayAdapter mAdapter;

    //--------------------------------------------------------------------------------------
    // Initialize
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Available Devices");

        // Init view
        setContentView(R.layout.activity_connection);
        mListView = (ListView) findViewById(R.id.device_list);

        // Init list
        devices = new ArrayList<>();
        devices = getIntent().getExtras().getStringArrayList("list");
        mAdapter = new ArrayAdapter<>(this, R.layout.device, devices);
        mListView.setAdapter(mAdapter);

        // Listens for which item is clicked
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceName = (String) mListView.getItemAtPosition(position);
                Intent data = new Intent();
                data.putExtra("deviceName", deviceName);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

}
