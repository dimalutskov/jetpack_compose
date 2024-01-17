package com.example.compose.jetchat.my

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothBaseViewModel : ViewModel(), BluetoothConnector.Listener {

    val bluetoothState: MutableLiveData<BluetoothConnector.State> = MutableLiveData(BluetoothConnector.State.NOT_SUPPORTED)

    val boundedDevices: MutableLiveData<MutableList<BluetoothDevice>> = MutableLiveData(ArrayList())

    val observedDevices: MutableLiveData<MutableList<BluetoothDevice>> = MutableLiveData(ArrayList())

    val isDeviceObserving: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onBluetoothStateChanged(state: BluetoothConnector.State) {
        bluetoothState.value = state
    }

    override fun onBluetoothDeviceObservingUpdate(observingStarted: Boolean) {
        isDeviceObserving.value = observingStarted
        if (observingStarted) {
            observedDevices.value = ArrayList()
        }
    }

    override fun onBluetoothDeviceFound(device: BluetoothDevice) {
        addObservedDevice(device)
    }

    override fun onBluetoothBoundStateChanged(device: BluetoothDevice, boundState: Int) {
        super.onBluetoothBoundStateChanged(device, boundState)
    }

    private fun addObservedDevice(device: BluetoothDevice) {
        val currentList = observedDevices.value.orEmpty().toMutableList()
        currentList.add(device)
        observedDevices.value = currentList
    }

}