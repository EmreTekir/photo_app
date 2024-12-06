package com.example.photo_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class PhotoHelper {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun Context.createImageFile(): File {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "JPEG_" + timestamp + "_"
            val image = File.createTempFile(imageFileName, ".jpg", externalCacheDir)
            return image
        }

        fun convertToBase64(uri: Uri, context: Context): String {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                print("Hata : " + e.message)
                return ""
            }
        }

        fun bitmapToBase64(bitmap: Bitmap, context: Context): String {
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                print("Hata : " + e.message)
                return ""
            }
        }
    }
}