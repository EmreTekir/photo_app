package com.example.photo_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.photo_app.PhotoHelper.Companion.createImageFile
import java.io.FileOutputStream
import java.util.Objects
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraService {
    @Composable
    fun CameraView(navController: NavHostController, photoViewModel: PhotoViewModel) {
        val lensFacing = CameraSelector.LENS_FACING_BACK
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val prewview = Preview.Builder().build()
        val previewView = remember { PreviewView(context) }
        val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        LaunchedEffect(lensFacing) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, prewview, imageCapture)
            prewview.setSurfaceProvider(previewView.surfaceProvider)
        }
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp
        val density = LocalDensity.current.density
        val screenWidthWithPadding = screenWidth - (screenWidth / 20)
        val buttonEnabled by photoViewModel.buttonEnabled.collectAsState()
        var boxPosition: IntOffset? = null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.8).dp)
                    .align(Alignment.Center)
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .width(screenWidthWithPadding.dp)
                        .height((screenWidthWithPadding / 1.8).dp)
                        .border(2.dp, Color.White)
                        .align(Alignment.Center)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInWindow()
                            //   println("pozisyon orjinal -> x : ${position.x} ; y : ${position.y}")
                            boxPosition = IntOffset(
                                (position.x / density).toInt(),
                                (position.y / density).toInt()
                            )
                            println("pozisyon yuvarlanmış -> x : ${boxPosition!!.x} ; y : ${boxPosition!!.y}")
                        }
                ) {}
            }

//            Box(
//                Modifier
//                    .size(20.dp)
//                    .align(Alignment.Center)
//                    .background(Color.White)
//            ) {}
//            Box(
//                Modifier
//                    .size(10.dp)
//                    .offset(9.dp, 332.dp)
//                    .background(Color.Red)
//            ) {}

            if (buttonEnabled) {
                IconButton(
                    enabled = buttonEnabled,
                    onClick = {
                        captureImage(
                            imageCapture,
                            context,
                            navController,
                            photoViewModel,
                            boxPosition,
                            screenWidthWithPadding,

                            )
                        photoViewModel.setButtonDisable()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(0.dp, 0.dp, 0.dp, 50.dp)
                        .size(60.dp),
                    colors = IconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Black
                    )
                ) {}
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(0.dp, 0.dp, 0.dp, 50.dp)
                        .size(60.dp),
                    color = Color.White
                )
            }

        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun captureImage(
        imageCapture: ImageCapture,
        context: Context,
        navController: NavHostController,
        photoViewModel: PhotoViewModel,
        boxPosition: IntOffset?,
        boxWidth: Int,

        ) {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val name = "JPEG_" + timestamp + "_"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Image")
//            }
//        }
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val file = context.createImageFile()
            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(context),
                "${context.packageName}.provider", file
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                file
            ).build()
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                        if (outputFileResults.savedUri != null) {
                            println("succes")
                            onCaptureSucces(
                                uri,
                                photoViewModel,
                                context,
                                boxWidth,
                                boxPosition,

                                )
                            navController.popBackStack()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        println("failed : " + exception.message)
                    }
                })
        }

    }

    fun onCaptureSucces(
        uri: Uri,
        photoViewModel: PhotoViewModel,
        context: Context,
        boxWidth: Int,
        boxPosition: IntOffset?,

        ) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val orginalbitmap = BitmapFactory.decodeStream(inputStream)

        val rotatedBitmap = rotateBitmap(orginalbitmap, 90f)
        val cropWidth = rotatedBitmap.width - (rotatedBitmap.width / 20)
        val croppedBitmap =
            Bitmap.createBitmap(
                rotatedBitmap,
                boxPosition!!.x,
                boxPosition.y,
                cropWidth,
                (cropWidth / 1.8).toInt()
            )


        val file = context.createImageFile()
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        photoViewModel.updatePhotoUri(Uri.fromFile(file))
        photoViewModel.updatePhotoBase64(croppedBitmap, context)
    }

    private fun rotateBitmap(bitmap: Bitmap, rotation: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this)
                .also { cameraProvider ->
                    cameraProvider.addListener({
                        continuation.resume(
                            cameraProvider.get()
                        )
                    }, ContextCompat.getMainExecutor(this))
                }
        }

}