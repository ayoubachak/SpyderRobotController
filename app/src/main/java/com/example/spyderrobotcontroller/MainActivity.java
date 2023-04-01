package com.example.spyderrobotcontroller;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;

    private Button forward, backwards, left, right,
                    climbing, sliding, grassWalk, inclinedWalk, normal,
                    speed1, speed2, speed3,
                    up, down, lock;


    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = BluetoothSocketHolder.getSocket();
    private OutputStream outputStream;

    private static final UUID MY_UUID = ScanActivity.MY_UUID;

    private ConnectThread connectThread;

    public void setClickListeners(){
        forward = findViewById(R.id.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("F");
            }
        });

        backwards = findViewById(R.id.backwards);
        backwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("B");
            }
        });

        left = findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("L");
            }
        });

        right = findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("R");
            }
        });

        climbing = findViewById(R.id.climbing);
        climbing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("C");
            }
        });

        sliding = findViewById(R.id.sliding);
        sliding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("S");
            }
        });

        grassWalk = findViewById(R.id.grasswalk);
        grassWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("GW");
            }
        });

        inclinedWalk = findViewById(R.id.inclinedwalk);
        inclinedWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("IW");
            }
        });

        normal = findViewById(R.id.normal);
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("N");
            }
        });

        speed1 = findViewById(R.id.spd1);
        speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("1");
            }
        });

        speed2 = findViewById(R.id.spd2);
        speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("2");
            }
        });

        speed3 = findViewById(R.id.spd3);
        speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("3");
            }
        });

        up = findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("U");
            }
        });

        down = findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("D");
            }
        });

        lock = findViewById(R.id.lock);
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChar("LCK");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setClickListeners();

        // get the bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            // enable bluetooth if it's not enabled
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void sendChar(String s){
        if (btSocket != null && btSocket.isConnected()) {
            sendData(s);
        } else {
            Toast.makeText(getApplicationContext(), "Not connected to device", Toast.LENGTH_SHORT).show();
        }
    }

    // method to send data to the connected device
    private void sendData(String message) {
        try {
            outputStream = btSocket.getOutputStream();
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error sending data", e);
        }
    }

    // method to connect to the selected device from ScanActivity
    public void connectDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    // thread to connect to the device
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Error creating socket", e);
            }
            btSocket = tmp;

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                btSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    btSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Error closing socket", closeException);
                }
                Log.e(TAG, "Error connecting to socket", e);
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }
}

