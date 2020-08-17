package com.martiandeveloper.muuvi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpWithPhoneOrEmailViewModel : ViewModel() {

    val phoneNumberETContent = MutableLiveData<String>()
    val emailETContent = MutableLiveData<String>()
    val confirmationCodeETContent = MutableLiveData<String>()
}
