package com.example.compose.jetchat.my

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.viewModels
import com.example.compose.jetchat.theme.Grey80

class BluetoothClientFragment : BluetoothBaseFragment() {

    private val viewModel: BluetoothClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.showConnectDialog.value == true) {
                    viewModel.showConnectDialog.value = false
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(inflater.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContent {
            setContent {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp, 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                        Button(
                            onClick = {
                                if (bluetoothConnector.discoverDevices()) {
                                    viewModel.showConnectDialog.value = true
                                }
                            }) {
                            Text(text = "Connect")
                        }
                    }

                    ConnectDeviceView()

                    BluetoothDisabledView()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        bluetoothConnector.subscribe(viewModel)
    }

    override fun onStop() {
        super.onStop()

        bluetoothConnector.unsubscribe(viewModel)
    }

    @Composable
    fun ConnectDeviceView() {
        val showConnectDialog by viewModel.showConnectDialog.observeAsState()

        if (showConnectDialog == true) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Column {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                    Text(
                        text = "Close",
                        Modifier.clickable { viewModel.showConnectDialog.value = false })

                    BluetoothDevicesList(Modifier)
                }
            }
        }
    }

    @Composable
    fun BluetoothDevicesList(modifier: Modifier) {
        val listItems = ArrayList<ListItem>()

        val pairedDevices = bluetoothConnector.getPairedDevices()
        if (pairedDevices.isNotEmpty()) {
            listItems.add(ListItem.TitleItem("Paired devices:", false))
            pairedDevices.forEach { listItems.add(ListItem.DeviceItem(it)) }
        }

        val showObservingProgress by baseViewModel.isDeviceObserving.observeAsState(false)

        listItems.add(ListItem.TitleItem("Available devices:", showObservingProgress))
        val observedDevices by baseViewModel.observedDevices.observeAsState(emptyList())
        observedDevices.forEach { listItems.add(ListItem.DeviceItem(it)) }

        // Maintain a reference to LazyListState
        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = modifier
                .padding(12.dp, 0.dp),
            state = lazyListState,
        ) {
            items(listItems.size) { index ->
                when (val item = listItems[index]) {
                    is ListItem.TitleItem -> TitleListItem(item.text, item.showProgress)
                    is ListItem.DeviceItem -> DeviceListItem(item.device)
                }
            }
        }
    }

    @Composable
    fun TitleListItem(title: String, showProgress: Boolean) {
        Row (
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 4.dp)
        ){
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .align(Alignment.CenterVertically),
                text = title,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp)

            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceListItem(device: BluetoothDevice) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 4.dp)
        ) {
            Text(
                text = device.name ?: "-",
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp)
            Text(
                text = device.address ?: "-",
                color = Grey80,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp)
        }
    }

    sealed class ListItem {
        data class TitleItem(val text: String, val showProgress: Boolean) : ListItem()
        data class DeviceItem(val device: BluetoothDevice) : ListItem()
    }
}