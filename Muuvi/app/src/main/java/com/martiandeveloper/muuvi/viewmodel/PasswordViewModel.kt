package com.martiandeveloper.muuvi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PasswordViewModel: ViewModel() {
    val passwordETContent = MutableLiveData<String>()
    val confirmPasswordET = MutableLiveData<String>()
}