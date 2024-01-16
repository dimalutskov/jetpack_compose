package com.example.compose.jetchat.my

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothServerViewModel : ViewModel(), BluetoothConnector.Listener {

    private val bluetoothConnector = BluetoothConnector()

    val bluetoothState: MutableLiveData<BluetoothConnector.State> = MutableLiveData(BluetoothConnector.State.NOT_SUPPORTED)

    // Use mutableStateOf to create a mutable state
    val messages: MutableLiveData<List<DisplayMessage>> = MutableLiveData(ArrayList())

    val inputText = MutableLiveData("")

    data class DisplayMessage(val type: MessageType, val sender: String, val message: String) {

        val time: Long = System.currentTimeMillis()

        enum class MessageType {
            CONNECTION, MESSAGE, ERROR
        }

    }

}