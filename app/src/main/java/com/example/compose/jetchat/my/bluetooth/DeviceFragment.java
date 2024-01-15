package com.example.compose.jetchat.my.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.nio.charset.StandardCharsets;

public class DeviceFragment extends Fragment {

    static String ARG_DEVICE = "device";

    private BluetoothDevice device;
    private DeviceConnectionThread connectionThread;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DeviceConnectionThread.MESSAGE_CONNECTED:
                    onConnected();
                    break;

                case DeviceConnectionThread.MESSAGE_ERROR:
                    Exception error = (Exception) msg.obj;
                    Toast.makeText(getActivity(), error.getClass() + " " + error.getMessage(), Toast.LENGTH_LONG).show();
                    break;

                case DeviceConnectionThread.MESSAGE_READ:
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    Toast.makeText(getActivity(), "RECEIVE: " + readMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getArguments().getParcelable(ARG_DEVICE);
        connect();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;//inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.findViewById(R.id.device_disconnect).setOnClickListener(v -> {
//            connectionThread.cancel();
            connectionThread.write("Hello".getBytes(StandardCharsets.UTF_8));
//        });
    }

    private void connect() {
        connectionThread = new DeviceConnectionThread(BluetoothAdapter.getDefaultAdapter(), handler, device);
        connectionThread.start();
    }

    private void onConnected() {
//        requireView().findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
