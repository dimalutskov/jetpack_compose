package com.example.compose.jetchat.my.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BluetoothServerFragment extends Fragment implements BluetoothServerThread.Listener {

    private BluetoothServerThread thread;

    private List<BluetoothDevice> connectedDevices = new ArrayList<>();

    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            thread = new BluetoothServerThread(this);
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onError(Exception e, @Nullable BluetoothDevice device) {
        handler.post(() -> {
            String name = device == null ? "SERVER" : device.getName();
            addMessage(new DisplayMessage(DisplayMessage.MessageType.ERROR, name, e.getClass() + " " + e.getMessage()));
        });
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceConnected(BluetoothDevice device) {
        handler.post(() -> {
            addMessage(new DisplayMessage(DisplayMessage.MessageType.CONNECTION, device.getName(), "connected to server!"));
            connectedDevices.add(device);
            refreshConnectedDevices();
        });

    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceDisconnected(BluetoothDevice device) {
        handler.post(() -> {
            addMessage(new DisplayMessage(DisplayMessage.MessageType.CONNECTION, device.getName(), "disconnected!"));
            connectedDevices.remove(device);
            refreshConnectedDevices();
        });
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceMessage(BluetoothDevice device, int length, byte[] bytes) {
        handler.post(() -> {
            String msgString = new String(bytes, StandardCharsets.UTF_8);
            addMessage(new DisplayMessage(DisplayMessage.MessageType.MESSAGE, device.getName(), msgString));
        });
    }

    @SuppressLint("MissingPermission")
    private void refreshConnectedDevices() {
        ViewGroup connectedDevicesContainer = getView().findViewById(R.id.connected_devices);
        connectedDevicesContainer.removeAllViews();

        for (BluetoothDevice device : connectedDevices) {
            View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_bluetooth_device, connectedDevicesContainer, false);
            ((TextView)item.findViewById(R.id.device_title)).setText(device.getName() + ": " + device.getAddress());
            connectedDevicesContainer.addView(item);
        }
    }

    private void addMessage(DisplayMessage message) {
        ViewGroup messagesContainer = getView().findViewById(R.id.server_messages_container);
        View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_server_message, messagesContainer, false);
        ((TextView)item.findViewById(R.id.message_content)).setText(message.type + " " + message.time + " " + message.message);
        messagesContainer.addView(item);
    }

    static class DisplayMessage {

        enum MessageType {
            CONNECTION,
            MESSAGE,
            ERROR
        }

        final MessageType type;
        final String sender;
        final String message;
        final long time;

        DisplayMessage(MessageType type, String sender, String message) {
            this.type = type;
            this.sender = sender;
            this.message = message;
            this.time = System.currentTimeMillis();
        }

    }
}
