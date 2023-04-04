package com.example.spyderrobotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT_PERMISSION = 2;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDeviceList);
        mListView.setAdapter(mAdapter);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Check for permission to connect to Bluetooth devices
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

        // Check for permission to scan for Bluetooth devices
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
        }

        // Set OnClickListener on the scan button
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan(null);
            }
        });

        // Set an item click listener for the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the BluetoothDevice for the selected item
                String item = (String) parent.getItemAtPosition(position);
                String address = item.substring(item.lastIndexOf("\n") + 1);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                // Attempt to connect to the device
                try {
                    if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT);
                    }

                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();

                    // saving the socket
                    BluetoothSocketHolder.setSocket(socket);
                    // If the connection succeeds, start the second activity
                    Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Get list of paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void scan(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Start discovery
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(ScanActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request missing permission
                    ActivityCompat.requestPermissions(ScanActivity.this,
                            new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_CONNECT_PERMISSION);
                } else {
                    mDeviceList.add(device.getName() + "\n" + device.getAddress());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_BLUETOOTH_CONNECT_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted for BLUETOOTH_CONNECT permission. Do the Bluetooth connection related task you need to do.
                } else {
                    // Permission denied for BLUETOOTH_CONNECT. Disable the functionality that depends on this permission.
                }
                break;
            }
            case REQUEST_ENABLE_BT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted for BLUETOOTH_SCAN permission. Scan for Bluetooth devices.
                    scan(null);
                } else {
                    // Permission denied for BLUETOOTH_SCAN. Disable the functionality that depends on this permission.
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
