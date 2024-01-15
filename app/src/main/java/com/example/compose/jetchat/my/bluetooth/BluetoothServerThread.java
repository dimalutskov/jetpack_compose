package com.example.compose.jetchat.my.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

class BluetoothServerThread extends Thread {

    static final String SERVER_NAME = "BluetoothServerThread";
    static final String MY_UUID = "80c35972-4a57-4905-8344-e74b5c92d0b4";

    interface Listener {
        void onError(Exception e, @Nullable BluetoothDevice device);
        void onDeviceConnected(BluetoothDevice device);
        void onDeviceDisconnected(BluetoothDevice device);
        void onDeviceMessage(BluetoothDevice device, int length, byte[] bytes);
    }

    private final BluetoothServerSocket mServerSocket;
    private final Listener mListener;

    @SuppressLint("MissingPermission")
    public BluetoothServerThread(Listener listener) throws IOException {
        mServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(SERVER_NAME, UUID.fromString(MY_UUID));
        mListener = listener;
    }

    public void run() {
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                BluetoothServerClient client = new BluetoothServerClient(mServerSocket.accept());
                client.start();
                mListener.onDeviceConnected(client.mSocket.getRemoteDevice());
            } catch (IOException e) {
                mListener.onError(e, null);
                break;
            }
        }
        cancel();
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) {}
    }

    class BluetoothServerClient extends Thread {
        final BluetoothSocket mSocket;
        final InputStream mInStream;
        final OutputStream mOutStream;

        byte[] mBuffer; // mBuffer store for the stream

        BluetoothServerClient(BluetoothSocket socket) throws IOException {
            this.mSocket = socket;
            this.mInStream = mSocket.getInputStream();
            this.mOutStream = mSocket.getOutputStream();
        }

        @Override
        public void run() {
            // Read data
            mBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    numBytes = mInStream.read(mBuffer);
                    mListener.onDeviceMessage(mSocket.getRemoteDevice(), numBytes, mBuffer);
                } catch (IOException e) {
                    mListener.onError(e, mSocket.getRemoteDevice());
                    mListener.onDeviceDisconnected(mSocket.getRemoteDevice());
                    break;
                }
            }
        }
    }
}
