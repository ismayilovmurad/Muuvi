package com.martiandeveloper.muuvi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpUsernameViewModel : ViewModel() {
    val usernameETContent = MutableLiveData<String>()
}