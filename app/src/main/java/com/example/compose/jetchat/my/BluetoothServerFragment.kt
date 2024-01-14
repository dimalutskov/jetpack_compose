package com.example.compose.jetchat.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Item 1")

                ChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1.0f)
                )

                Text(
                    modifier = Modifier
                        .clickable { viewModel.addItem() },
                    text = "Item 3")
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
            state = lazyListState
        ) {
            items(itemList.size) { index ->
                ChatListItem(data = itemList[index])

                println("@@@ DRAW ITEM " + index)
            }
        }

        // Trigger scrolling when the list changes
        LaunchedEffect(itemList) {
            println("@@@ LAUNCHED EFFECT " + itemList.size)
//            val previousData = snapshotState().prev
//            val currentData = snapshotState().value
            // Scroll to the bottom after adding the new item
            lazyListState.scrollToItem(itemList.size - 1)
        }
    }

    @Composable
    fun ChatListItem(data: BluetoothServerViewModel.CustomData) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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

}