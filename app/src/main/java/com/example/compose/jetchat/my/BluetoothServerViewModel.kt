package com.example.compose.jetchat.my

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothServerViewModel : ViewModel(),
    BluetoothServerThread.Listener {

    val connectedDevices: MutableLiveData<MutableList<BluetoothDevice>> = MutableLiveData(ArrayList())

    val messages: MutableLiveData<MutableList<DisplayMessage>> = MutableLiveData(ArrayList())

    val inputText = MutableLiveData("")

    override fun onServerStarted() {
        viewModelScope.launch(Dispatchers.Main) {
            addMessage(DisplayMessage(
                DisplayMessage.MessageType.CONNECTION,
                "SERVER",
                "Started!")
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onError(e: Exception, device: BluetoothDevice?) {
        viewModelScope.launch(Dispatchers.Main) {
            val name = if (device == null) "SERVER" else device.name
            addMessage(DisplayMessage(
                DisplayMessage.MessageType.ERROR,
                name,
                e.javaClass.toString() + " " + e.message)
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceConnected(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.Main) {
            connectedDevices.value?.add(device)
            addMessage(DisplayMessage(
                DisplayMessage.MessageType.CONNECTION,
                device.name,
                "Connected!")
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceDisconnected(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.Main) {
            connectedDevices.value?.remove(device)
            addMessage(DisplayMessage(
                DisplayMessage.MessageType.CONNECTION,
                device.name,
                "Disconnected!")
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceMessage(device: BluetoothDevice, length: Int, bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.Main) {
            val msgString = String(bytes, 0, length, Charsets.UTF_8)
            addMessage(DisplayMessage(
                DisplayMessage.MessageType.MESSAGE,
                device.name,
                msgString)
            )
        }
    }

    private fun addMessage(message: DisplayMessage) {
        val currentList = messages.value.orEmpty().toMutableList()
        currentList.add(message)
        messages.value = currentList
    }

    data class DisplayMessage(val type: MessageType, val sender: String, val message: String) {

        val time: Long = System.currentTimeMillis()

        enum class MessageType {
            CONNECTION, MESSAGE, ERROR
        }

    }

}