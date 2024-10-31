package com.fsck.k9.ui.messagelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageListActivityViewModel: ViewModel() {
    var eventPurchaseStateChange: MutableLiveData<Boolean> = MutableLiveData()

    fun senEventPurchaseStateChange(isPurchased: Boolean) {
        eventPurchaseStateChange.value = isPurchased
    }
}
