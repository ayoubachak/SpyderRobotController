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
import android.widget.TextView;
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

    private String lockedval = "Unlocked";
    private String walkingmodeval = "Normal";
    private String speedval = "Speed 1";
    private String direction = " ";
    private TextView lockedTextView, walkingTextView, speedTextView, directionTextView;


    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = BluetoothSocketHolder.getSocket();
    private OutputStream outputStream;

    private static final UUID MY_UUID = ScanActivity.MY_UUID;

    private ConnectThread connectThread;

    public void setClickListeners(){
        lockedTextView = findViewById(R.id.lockedval);
        walkingTextView = findViewById(R.id.walkingmodeval);
        speedTextView = findViewById(R.id.speedval);
        directionTextView = findViewById(R.id.dir);

        forward = findViewById(R.id.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("F")){
                    direction = "F";
                }
                updateValues();
            }
        });

        backwards = findViewById(R.id.backwards);
        backwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("B")){
                    direction = "B";
                }
                updateValues();
            }
        });

        left = findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("L")){
                    direction = "L";
                }
                updateValues();
            }
        });

        right = findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("R")){
                    direction = "R";
                }
                updateValues();
            }
        });

        climbing = findViewById(R.id.climbing);
        climbing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("C")){
                    walkingmodeval = "Climbing";
                }
                updateValues();
            }
        });

        sliding = findViewById(R.id.sliding);
        sliding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("S")){
                    walkingmodeval = "Sliding";
                }
                updateValues();
            }
        });

        grassWalk = findViewById(R.id.grasswalk);
        grassWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("GW")){
                    walkingmodeval = "Grass Walk";
                }
                updateValues();
            }
        });

        inclinedWalk = findViewById(R.id.inclinedwalk);
        inclinedWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("IW")){
                    walkingmodeval = "Inclined Walk";
                }
                updateValues();
            }
        });

        normal = findViewById(R.id.normal);
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("N")){
                    walkingmodeval = "Normal";
                }
                updateValues();
            }
        });

        speed1 = findViewById(R.id.spd1);
        speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("1")){
                    speedval = "Speed 1";
                }
                updateValues();
            }
        });

        speed2 = findViewById(R.id.spd2);
        speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("2")){
                    speedval = "Speed 2";
                }
                updateValues();
            }
        });

        speed3 = findViewById(R.id.spd3);
        speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("3")){
                    speedval = "Speed 3";
                }
                updateValues();
            }
        });

        up = findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("U")){
                    direction = "Up";
                }
                updateValues();
            }
        });

        down = findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("D")){
                    direction = "Dwn";
                }
                updateValues();
            }
        });

        lock = findViewById(R.id.lock);
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendChar("LK")){
                    lockedval = lockedval == "Unlocked"?"Locked":"Unlocked";
                }
                updateValues();
            }
        });
    }

    public void updateValues(){
        lockedTextView.setText(lockedval);
        directionTextView.setText(direction);
        walkingTextView.setText(walkingmodeval);
        speedTextView.setText(speedval);
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

    public boolean sendChar(String s){
        if (btSocket != null && btSocket.isConnected()) {
            return sendData(s);
        } else {
            Toast.makeText(getApplicationContext(), "Not connected to device", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // method to send data to the connected device
    private boolean sendData(String message) {
        try {
            outputStream = btSocket.getOutputStream();
            outputStream.write(message.getBytes());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error sending data", e);
        }
        return false;
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

