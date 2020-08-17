package com.martiandeveloper.muuvi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogInViewModel : ViewModel() {

    val phoneNumberEmailUsernameACTContent = MutableLiveData<String>()
    val passwordETContent = MutableLiveData<String>()
    val confirmationCodeETContent = MutableLiveData<String>()
}
