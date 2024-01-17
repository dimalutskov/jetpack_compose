package com.example.compose.jetchat.my

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothClientViewModel : ViewModel(),
    BluetoothConnector.Listener {

    var showConnectDialog : MutableLiveData<Boolean> = MutableLiveData(false)

}