package com.example.compose.jetchat.my

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.CopyOnWriteArrayList


class BluetoothConnector {

    private val listeners = CopyOnWriteArrayList<Listener>()

    private lateinit var activity: FragmentActivity
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
    private lateinit var requestBluetoothLauncher: ActivityResultLauncher<Intent>
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    listeners.forEach { it.onBluetoothStateChanged(bluetoothAdapter?.isEnabled == true) }
                }

                BluetoothDevice.ACTION_FOUND -> {
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let { device ->
                        listeners.forEach { it.onBluetoothDeviceFound(device) }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    stopDiscoveringDevices()
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    intent.extras?.let { extras ->
                        val device = extras.getParcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceBondState = extras.getInt(BluetoothDevice.EXTRA_BOND_STATE)
                        listeners.forEach { it.onBluetoothBoundStateChanged(device!!, deviceBondState) }
                    }
                }
            }
        }
    }

    fun init(fragment: Fragment) {
        val bluetoothManager = fragment.requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        this.activity = fragment.requireActivity()
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        fragment.activity?.registerReceiver(receiver, filter)

        requestBluetoothLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            listeners.forEach { it.onBluetoothStateChanged(bluetoothAdapter?.isEnabled == true) }
        }

        requestPermissionsLauncher = fragment.registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                checkState()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
    }

    fun destroy() {
        stopDiscoveringDevices()
        activity.unregisterReceiver(receiver)
    }

    fun subscribe(listener: Listener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: Listener) {
        listeners.remove(listener)
    }

    fun checkState(): State {
        if (bluetoothAdapter == null) {
            return State.NOT_SUPPORTED
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            Toast.makeText(activity, "Check ACCESS_FINE_LOCATION permission", Toast.LENGTH_LONG).show()
            return State.REQUIRE_LOCATION_PERMISSION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            Toast.makeText(activity, "Check BLUETOOTH_CONNECT permission", Toast.LENGTH_LONG).show()
            return State.REQUIRE_BLUETOOTH_PERMISSION
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothLauncher.launch(enableBtIntent)
            return State.BLUETOOTH_DISABLED
        }
        return State.BLUETOOTH_ENABLED
    }

    private fun discoverDevices(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
            Toast.makeText(activity, "Check BLUETOOTH_SCAN permission", Toast.LENGTH_LONG).show()
            return false
        }
        if (bluetoothAdapter?.isEnabled == true && bluetoothAdapter?.isDiscovering == false) {
            bluetoothAdapter?.startDiscovery()
            return true
        }
        return false
    }

    fun stopDiscoveringDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
            }
        }
    }

    fun getPairedDevices(): Collection<BluetoothDevice> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        return bluetoothAdapter?.bondedDevices ?: emptyList()
    }

    interface Listener {
        fun onBluetoothStateChanged(enabled: Boolean) {}
        fun onBluetoothDeviceFound(device: BluetoothDevice) {}
        fun onBluetoothBoundStateChanged(device: BluetoothDevice, boundState: Int) {}
    }

    enum class State {
        NONE,
        NOT_SUPPORTED,
        REQUIRE_LOCATION_PERMISSION,
        REQUIRE_BLUETOOTH_PERMISSION,
        BLUETOOTH_DISABLED,
        BLUETOOTH_ENABLED
    }

}