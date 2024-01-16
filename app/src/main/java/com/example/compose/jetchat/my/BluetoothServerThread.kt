package com.example.compose.jetchat.my

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothServerThread(
    private val adapter: BluetoothAdapter,
    private val listener: Listener) : Thread() {

    lateinit var serverSocket: BluetoothServerSocket

    @SuppressLint("MissingPermission")
    override fun start() {
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVER_NAME, UUID.fromString(MY_UUID))
            super.start()
        } catch (e: IOException) {
            listener.onError(e, null)
        }

    }

    override fun run() {
        listener.onServerStarted()
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                val client = BluetoothServerClient(serverSocket.accept())
                client.start()
                listener.onDeviceConnected(client.socket.remoteDevice)
            } catch (e: IOException) {
                listener.onError(e, null)
                break
            }
        }
        cancel()
    }

    // Closes the connect socket and causes the thread to finish.
    fun cancel() {
        try {
            serverSocket.close()
        } catch (_: IOException) {}
    }

    inner class BluetoothServerClient(val socket: BluetoothSocket) : Thread() {
        val inStream: InputStream
        val outStream: OutputStream
        val buffer = ByteArray(1024)

        init {
            inStream = socket.inputStream
            outStream = socket.outputStream
        }

        override fun run() {
            listener.onServerStarted()
            // Read data
            var numBytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    numBytes = inStream.read(buffer)
                    listener.onDeviceMessage(socket.remoteDevice, numBytes, buffer)
                } catch (e: IOException) {
                    listener.onError(e, socket.remoteDevice)
                    listener.onDeviceDisconnected(socket.remoteDevice)
                    break
                }
            }
        }
    }

    interface Listener {
        fun onServerStarted()
        fun onError(e: Exception, device: BluetoothDevice?)
        fun onDeviceConnected(device: BluetoothDevice)
        fun onDeviceDisconnected(device: BluetoothDevice)
        fun onDeviceMessage(device: BluetoothDevice, length: Int, bytes: ByteArray)
    }

    companion object {
        val SERVER_NAME = "BluetoothServerThread"
        val MY_UUID = "80c35972-4a57-4905-8344-e74b5c92d0b4"
    }

}