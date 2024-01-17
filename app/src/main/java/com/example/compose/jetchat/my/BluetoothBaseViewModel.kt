package com.example.compose.jetchat.my

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothBaseViewModel : ViewModel(), BluetoothConnector.Listener {

    val bluetoothState: MutableLiveData<BluetoothConnector.State> = MutableLiveData(BluetoothConnector.State.NOT_SUPPORTED)

    override fun onBluetoothStateChanged(state: BluetoothConnector.State) {
        bluetoothState.value = state
    }

}