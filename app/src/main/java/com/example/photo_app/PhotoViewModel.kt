package com.example.photo_app

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import coil3.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PhotoViewModel : ViewModel() {
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> get() = _photoUri

    private val _photoBase64 = MutableStateFlow<String>("")
    val photoBase64: StateFlow<String> get() = _photoBase64

    private val _buttonEnabled = MutableStateFlow<Boolean>(true)
    val buttonEnabled: StateFlow<Boolean> get() = _buttonEnabled


    fun setButtonDisable() {
        _buttonEnabled.value = false
    }

    fun setButtonEnable() {
        _buttonEnabled.value = true
    }

    fun updatePhotoUri(newUri: Uri) {
        _photoUri.value = newUri
    }

    fun updatePhotoBase64(bitmap: Bitmap, context: Context) {
        _photoBase64.value = PhotoHelper.bitmapToBase64(bitmap, context)
    }
}