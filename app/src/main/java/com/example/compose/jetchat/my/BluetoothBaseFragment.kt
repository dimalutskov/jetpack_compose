package com.example.compose.jetchat.my

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

open class BluetoothBaseFragment : Fragment() {

    protected val bluetoothConnector = BluetoothConnector()

    protected val baseViewModel: BluetoothBaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothConnector.init(this)
    }

    override fun onStart() {
        super.onStart()

        bluetoothConnector.subscribe(baseViewModel)
        baseViewModel.bluetoothState.value = bluetoothConnector.checkState()
    }

    override fun onStop() {
        super.onStop()
        bluetoothConnector.unsubscribe(baseViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnector.destroy()
    }

    @Composable
    fun BluetoothDisabledView() {
        val state by baseViewModel.bluetoothState.observeAsState()
        if (state != BluetoothConnector.State.BLUETOOTH_ENABLED) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Text(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        text = "Bluetooth disabled")
                    Text(
                        fontSize = 16.sp,
                        text = "Check app permissions"
                    )
                }

            }
        }
    }

}