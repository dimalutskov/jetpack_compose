package com.example.compose.jetchat.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.compose.jetchat.theme.Blue80

class BluetoothServerFragment : Fragment() {

    private val bluetoothConnector = BluetoothConnector()

    private var bluetoothServerThread: BluetoothServerThread? = null

    private val viewModel: BluetoothServerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothConnector.init(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(inflater.context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContent {
            VerticalLinearLayout()
        }
    }

    override fun onStart() {
        super.onStart()

        bluetoothConnector.subscribe(viewModel)
        viewModel.bluetoothState.value = bluetoothConnector.checkState()
        viewModel.bluetoothState.observe(viewLifecycleOwner) {
            checkServerThread()
        }
        checkServerThread()
    }

    override fun onStop() {
        super.onStop()
        bluetoothConnector.unsubscribe(viewModel)
        bluetoothServerThread?.cancel()
    }

    fun checkServerThread() {
        bluetoothConnector.bluetoothAdapter?.let { adapter ->
            if (adapter.isEnabled && (bluetoothServerThread == null || bluetoothServerThread?.isAlive != true)) {
                bluetoothServerThread = BluetoothServerThread(adapter, viewModel)
                bluetoothServerThread?.start()
            } else if (!adapter.isEnabled && bluetoothServerThread?.isAlive == true) {
                bluetoothServerThread?.cancel()
                bluetoothServerThread = null
            } else {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnector.destroy()
    }

    @Composable
    fun VerticalLinearLayout() {

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

                Text(text = "Item 1")

                ChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1.0f)
                        .border(1.dp, Blue80, RoundedCornerShape(4.dp))
                )

                ChatSendContainer()
            }

            DisabledView()
        }
    }

    @Composable
    fun DisabledView() {
        val state by viewModel.bluetoothState.observeAsState()
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

    @Composable
    fun ChatList(modifier: Modifier) {
        val itemList by viewModel.messages.observeAsState(emptyList())

        // Maintain a reference to LazyListState
        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            items(itemList.size) { index ->
                ChatListItem(data = itemList[index])
            }
        }

        LaunchedEffect(itemList) {
            if (itemList.isNotEmpty()) {
                lazyListState.scrollToItem(itemList.size - 1)
            }
        }
    }

    @Composable
    fun ChatListItem(data: BluetoothServerViewModel.DisplayMessage) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 12.dp)
                .background(Color.Gray)
        ) {
            Text(text = data.message, color = Color.White)
        }
    }

    @Composable
    fun ChatSendContainer() {
        val textState by viewModel.inputText.observeAsState()

        Row {
            OutlinedTextField(
                value = textState ?: "",
                onValueChange = {
                    // Update the text state in the ViewModel
                    viewModel.inputText.value = it
                },
                label = { Text("Enter Text") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    // Hide the keyboard when Done is pressed
//                    keyboardController?.hide()
//                }
//            ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                modifier = Modifier
                    .padding(0.dp, 6.dp, 0.dp, 0.dp)
                    .height(42.dp)
                    .align(Alignment.CenterVertically)
                ,
                enabled = textState?.isNotEmpty() == true,
                onClick = { },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Send",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        // Use LocalSoftwareKeyboardController to show/hide keyboard
//        val keyboardController = LocalSoftwareKeyboardController.current




    }

}