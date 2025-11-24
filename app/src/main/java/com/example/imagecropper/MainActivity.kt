package com.example.imagecropper

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cropperlibrary.ImageCropper
import com.example.imagecropper.ui.theme.ImageCropperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageCropperTheme {
                Scaffold { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(10.dp)
                    ) {

                        var selectedUri by remember { mutableStateOf<Uri?>(null) }
                        var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                selectedUri = uri
                                selectedBitmap = null
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            selectedUri?.let { uri ->
                                ImageCropper(
                                    imageUri = uri,
                                    cropWidth = 600f, //options for crop width
                                    cropHeight = 750f, //options for crop height
                                    onCropComplete = { bitmap ->
                                        selectedBitmap = bitmap
                                        selectedUri = null
                                    },
                                    onCancel = {
                                        //cancel cropping
                                        selectedUri = null
                                    })
                            }
                            if (selectedBitmap != null) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Button(onClick = { launcher.launch("image/*") }) {
                            Text("pick image")
                        }
                    }
                }

            }
        }
    }
}




