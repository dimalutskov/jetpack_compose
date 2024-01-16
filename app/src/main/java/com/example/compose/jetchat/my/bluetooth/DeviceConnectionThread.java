package com.example.compose.jetchat.my.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DeviceConnectionThread  extends Thread {

    public static final int MESSAGE_ERROR = -1;
    public static final int MESSAGE_CONNECTED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;

    private InputStream mInStream;
    private OutputStream mOutStream;
    private byte[] mBuffer; // mBuffer store for the stream

    @SuppressLint("MissingPermission")
    public DeviceConnectionThread(BluetoothAdapter adapter, Handler handler, BluetoothDevice device) {
        mAdapter = adapter;
        mHandler = handler;
        mDevice = device;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
//            ParcelUuid[] idArray = device.getUuids();
//            java.util.UUID uuidYouCanUse = java.util.UUID.fromString(idArray[0].toString());
            UUID uuid = UUID.randomUUID();//UUID.fromString(BluetoothServerThread.MY_UUID);
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
            mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, e).sendToTarget();
        }
        mSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        // Connection
        try {
            mSocket.connect();
        } catch (Exception connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {}
            mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, connectException).sendToTarget();
            return;
        }

        //
        try {
            mInStream = mSocket.getInputStream();
        } catch (IOException e) {
            mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, e).sendToTarget();
        }
        try {
            mOutStream = mSocket.getOutputStream();
        } catch (IOException e) {
            mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, e).sendToTarget();
        }

        mHandler.obtainMessage(MESSAGE_CONNECTED, -1, -1, null).sendToTarget();

        // Read data
        mBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mInStream.read(mBuffer);
                // Send the obtained bytes to the UI activity.
                mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, mBuffer).sendToTarget();
            } catch (IOException e) {
                mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, e).sendToTarget();
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            mOutStream.write(bytes);
            mOutStream.flush();

            // Share the sent message with the UI activity.
            Message writtenMsg = mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, mBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            mHandler.obtainMessage(MESSAGE_ERROR, -1, -1, e).sendToTarget();
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            // TODO
        }
    }
}
