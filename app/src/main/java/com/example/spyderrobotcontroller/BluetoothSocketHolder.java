package com.example.spyderrobotcontroller;

import android.bluetooth.BluetoothSocket;

public class BluetoothSocketHolder {
    private static BluetoothSocket socket;

    public static BluetoothSocket getSocket() {
        return socket;
    }

    public static void setSocket(BluetoothSocket socket) {
        BluetoothSocketHolder.socket = socket;
    }
}
