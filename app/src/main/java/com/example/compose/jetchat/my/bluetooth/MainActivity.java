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

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_CODE_LOCATION = 101;
    private static final int REQUEST_CODE_ENABLE_BT = 102;
    private static final int REQUEST_CODE_BT_CONNECT_PERMISSION = 103;
    private static final int REQUEST_CODE_BT_SCAN_PERMISSION = 104;

    // Used to load the 'bluetoothapplication' library on application startup.
//    static {
//        System.loadLibrary("bluetoothapplication");
//    }

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {

                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    updateBluetoothState();
                    break;
                }

                case BluetoothDevice.ACTION_FOUND:
                    onBluetoothDeviceFound(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                    stopObservingDevices();
                    break;
                }

                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    Toast.makeText(MainActivity.this, "ACTION_PAIRING_REQUEST", Toast.LENGTH_LONG).show();
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    onBondStateChanged(intent);
                    break;

            }
        }
    };



    private DeviceConnectionThread connectionThread;

//    private ActivityMainBinding binding;

    private ProgressDialog pairingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter);

//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        binding.bluetoothSwitch.setOnCheckedChangeListener(this);
//        binding.btnFindDevices.setOnClickListener(v -> discoverDevices());
//        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateBluetoothState();

        // TODO
//        ProfilesFragment fragment = new ProfilesFragment();
//        getSupportFragmentManager().beginTransaction().add(R.id.activity_root, fragment).commitNowAllowingStateLoss();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopObservingDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ENABLE_BT) {
            updateBluetoothState();
        }
    }

    private boolean checkPreconditions() {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Check ACCESS_FINE_LOCATION permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, // TODO ACCESS_COARSE
                    REQUEST_CODE_LOCATION);
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Check BLUETOOTH_CONNECT permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_CODE_BT_CONNECT_PERMISSION);
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Check BLUETOOTH_SCAN permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_CODE_BT_SCAN_PERMISSION);
            return false;
        }

        return true;
    }

    private void toggleBluetoothSwitch(boolean checked) {
//        binding.bluetoothSwitch.setOnCheckedChangeListener(null);
//        binding.bluetoothSwitch.setChecked(checked);
//        binding.bluetoothSwitch.setOnCheckedChangeListener(this);
    }

    @SuppressLint("MissingPermission")
    private void onBondStateChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        final BluetoothDevice device = extras.getParcelable(BluetoothDevice.EXTRA_DEVICE);
        final int deviceBondState = extras.getInt(BluetoothDevice.EXTRA_BOND_STATE);

        if (deviceBondState == BluetoothDevice.BOND_BONDING) {
            pairingProgress = new ProgressDialog(MainActivity.this);
            pairingProgress.setMessage("Pairing with " + device.getName());
            pairingProgress.setTitle("Pair processing");
            pairingProgress.show();
        } else {
            if (pairingProgress != null) {
                pairingProgress.dismiss();
            }
            updatePairedDevices();

            String msg = deviceBondState == BluetoothDevice.BOND_BONDED ? "Device " + device.getName() + " successfully paired"
                    : "Failed to pair with " + device.getName();
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (!checkPreconditions()) {
            toggleBluetoothSwitch(!checked);
            return;
        }

        if (checked) {
            if (!bluetoothAdapter.isEnabled()) {
                toggleBluetoothSwitch(false);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT);
            } else {
                updateBluetoothState();
            }
        } else {
            stopObservingDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void updateBluetoothState() {
        if (!checkPreconditions()) {
            stopObservingDevices();
            return;
        }

        toggleBluetoothSwitch(bluetoothAdapter.isEnabled());
        updatePairedDevices();
    }

    @SuppressLint("MissingPermission")
    private void discoverDevices() {
//        if (bluetoothAdapter.isEnabled() && !bluetoothAdapter.isDiscovering()) {
//            binding.titleAvailableDevices.setVisibility(View.GONE);
//            binding.bluetoothDevicesContainer.removeAllViews();
//            bluetoothAdapter.startDiscovery();
//            binding.findDevices.setVisibility(View.VISIBLE);
//            binding.btnFindDevices.setVisibility(View.GONE);
//        } else if (!bluetoothAdapter.isEnabled()) {
//            stopObservingDevices();
//        }
        startBluetoothServer();
    }

    private void stopObservingDevices() {
//        binding.findDevices.setVisibility(View.GONE);
//        binding.btnFindDevices.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    private void updatePairedDevices() {
//        binding.bluetoothPairedDevicesContainer.removeAllViews();
//        binding.titlePairedDevices.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
//            binding.titlePairedDevices.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
//                BluetoothDeviceItem item = new BluetoothDeviceItem(device, binding.bluetoothPairedDevicesContainer);
//                binding.bluetoothPairedDevicesContainer.addView(item.view);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void onBluetoothDeviceFound(BluetoothDevice device) {
        if (device.getName() != null) {
//            binding.titleAvailableDevices.setVisibility(View.VISIBLE);
//            BluetoothDeviceItem item = new BluetoothDeviceItem(device, binding.bluetoothDevicesContainer);
//            binding.bluetoothDevicesContainer.addView(item.view);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            updateBluetoothState();
        }
    }

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
                stopObservingDevices();
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