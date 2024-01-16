package com.example.compose.jetchat.my.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import java.nio.charset.StandardCharsets;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private DeviceConnectionThread connectionThread;

//    private ActivityMainBinding binding;

    private ProgressDialog pairingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        binding.bluetoothSwitch.setOnCheckedChangeListener(this);
//        binding.btnFindDevices.setOnClickListener(v -> discoverDevices());
//        setContentView(binding.getRoot());
    }

    private void toggleBluetoothSwitch(boolean checked) {
//        binding.bluetoothSwitch.setOnCheckedChangeListener(null);
//        binding.bluetoothSwitch.setChecked(checked);
//        binding.bluetoothSwitch.setOnCheckedChangeListener(this);
    }


//    @SuppressLint("MissingPermission")
//    private void updateBluetoothState() {
//        if (!checkPreconditions()) {
//            stopObservingDevices();
//            return;
//        }
//
//        toggleBluetoothSwitch(bluetoothAdapter.isEnabled());
//        updatePairedDevices();
//    }







    // TEMP
    void startBluetoothServer() {
//        BluetoothServerFragment fragment = new BluetoothServerFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        getSupportFragmentManager().beginTransaction().add(R.id.activity_root, fragment).commitNowAllowingStateLoss();
    }

    class BluetoothDeviceItem {
        final BluetoothDevice device;
        final View view;

        @SuppressLint("MissingPermission")
        BluetoothDeviceItem(BluetoothDevice device, ViewGroup parent) {
            this.device = device;
            this.view = null;//LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
//            ((TextView)view.findViewById(R.id.device_title)).setText(device.getName() + " (" + device.getBluetoothClass().getDeviceClass() + ") : " + device.getAddress());
            this.view.setOnClickListener(view -> {
//                stopObservingDevices();
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    pairDevice();
                } else {
                    connectDevice();
                }
            });
        }

        @SuppressLint("MissingPermission")
        private void pairDevice() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Confirm pair with " + device.getName())
                    .setTitle("Pair new device")
                    .setPositiveButton("Pair", (dialog, id) -> device.createBond())
                    .setNegativeButton("Cancel", (dialog, id) -> {})
                    .create().show();
        }

        @SuppressLint("MissingPermission")
        private void connectDevice() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Confirm connect to " + device.getName())
                    .setTitle("Connect device")
                    .setPositiveButton("Connect", (dialog, id) -> {
//                        DeviceFragment fragment = new DeviceFragment();
//                        Bundle args = new Bundle();
//                        args.putParcelable(DeviceFragment.ARG_DEVICE, device);
//                        fragment.setArguments(args);
//                        getSupportFragmentManager().beginTransaction().add(R.id.activity_root, fragment).commitNowAllowingStateLoss();
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {})
                    .create().show();
        }
    }

}