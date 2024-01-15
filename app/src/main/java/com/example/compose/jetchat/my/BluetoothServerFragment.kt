package com.example.compose.jetchat.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.compose.jetchat.theme.Blue80

class BluetoothServerFragment : Fragment() {

    private val viewModel: BluetoothServerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setContent {
            VerticalLinearLayout()
        }
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
        }
    }

    @Composable
    fun ChatList(modifier: Modifier) {
        val itemList by viewModel.itemList.observeAsState(emptyList())

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
            lazyListState.scrollToItem(itemList.size - 1)
        }
    }

    @Composable
    fun ChatListItem(data: BluetoothServerViewModel.CustomData) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 12.dp)
                .background(Color.Gray)
        ) {
            Text(text = data.title, color = Color.White)
            Text(text = data.subtitle, color = Color.White)
            when (data.icon) {
                "info" -> Icons.Default.Info
                "star" -> Icons.Default.Star
                "warning" -> Icons.Default.Warning
                else -> {}
            }.let { icon ->
                Text(
                    text = "Icon: $icon",
                    color = Color.White
                )
            }
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