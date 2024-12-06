package com.example.photo_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.photo_app.ui.theme.Photo_appTheme


class MainActivity : ComponentActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()
    private val cameraService = CameraService()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { App(navController) }
                composable("camera") { cameraService.CameraView(navController, photoViewModel) }
            }
        }
    }


    @Composable
    fun App(navController: NavHostController) {
        val context = LocalContext.current
        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Toast.makeText(context, "Kamera İzni Verildi", Toast.LENGTH_SHORT).show()
                    navController.navigate("camera")
                } else {
                    Toast.makeText(context, "Kamera İzni Verilmedi", Toast.LENGTH_SHORT).show()
                }
            }
        val scrollState = rememberScrollState()
        Photo_appTheme {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
            ) { innerPadding ->
                Column(
                    Modifier
                        .padding(innerPadding)
                        .padding(20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Blue)
                            .clickable {
                                val permissionChceck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )
                                if (permissionChceck == PackageManager.PERMISSION_GRANTED) {
                                    photoViewModel.setButtonEnable()
                                    navController.navigate("camera")
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Fotoğraf Çek", style = TextStyle(
                                color = Color.White,
                                fontSize = 30.sp
                            )
                        )
                    }
                    val photo by photoViewModel.photoUri.collectAsState()
                    val photoBs64 by photoViewModel.photoBase64.collectAsState()
                    if (photo != null) {
                        AsyncImage(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            model = photo,
                            contentDescription = null,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(photoBs64, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
