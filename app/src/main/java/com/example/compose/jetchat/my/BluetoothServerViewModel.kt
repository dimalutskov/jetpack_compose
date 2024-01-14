package com.example.compose.jetchat.my

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothServerViewModel : ViewModel() {

    data class CustomData(
        val title: String,
        val subtitle: String,
        val icon: String // In a real-world scenario, you might want to use a more appropriate data type for icons
    )

    // Use mutableStateOf to create a mutable state
    val itemList: MutableLiveData<List<CustomData>>

    // Function to update the itemList
    fun updateItemList(newList: List<CustomData>) {
        itemList.value = newList
    }

    fun addItem(item: CustomData) {
        itemList.value = itemList.value?.plus(item)
    }

    fun addItem() {
        addItem(CustomData("New title", "B", "C"))
    }

    init {
        // TEST
        val dummyItemList = List(10) {
            BluetoothServerViewModel.CustomData(
                "Title $it", "Subtitle $it", when {
                    it % 3 == 0 -> "info"
                    it % 3 == 1 -> "star"
                    else -> "warning"
                }
            )
        }
        itemList = MutableLiveData<List<CustomData>>(dummyItemList)
    }


}