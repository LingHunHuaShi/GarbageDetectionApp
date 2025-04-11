package com.zzh.garbagedetection.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzh.garbagedetection.R
import com.zzh.garbagedetection.data.SettingsViewModel
import com.zzh.garbagedetection.detection.Detection
import com.zzh.garbagedetection.ui.components.ScaledDetectionImage
import com.zzh.garbagedetection.data.ModelNameEnums.YOLO
import com.zzh.garbagedetection.data.ModelNameEnums.GOOGLE
import com.zzh.garbagedetection.detection.detectImageGoogle
import com.zzh.garbagedetection.detection.detectImageYolo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetectPageContainer(viewModel: SettingsViewModel = viewModel(), modifier: Modifier = Modifier) {
    val modelName = viewModel.modelName.collectAsState().value
    val threshold = viewModel.threshold.collectAsState().value
    val context = LocalContext.current
    var displayImage by remember { mutableStateOf<Bitmap?>(null) }
    var detectionList by remember { mutableStateOf(listOf<Detection>()) }
    var isDetecting by remember { mutableStateOf(false) }
    var isDetectionDone by remember { mutableStateOf(false) }


    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            Log.d("Photo taker launcher", "Successfully taken photo!")
            displayImage = it
        }
    val photosLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                displayImage = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
        }

    val scope = rememberCoroutineScope()

    DetectPage(
        displayImage,
        detectionList,
        isDetecting,
        detectBtnOnClick = {
            isDetecting = true
            scope.launch(Dispatchers.Default) {
                val results = when (modelName) {
                    YOLO.label -> detectImageYolo(
                        inputImage = displayImage!!,
                        threshold = threshold,
                        context = context
                    )

                    GOOGLE.label -> detectImageGoogle(
                        inputImage = displayImage!!,
                        threshold = threshold,
                        context = context
                    )

                    else -> emptyList()
                }
                withContext(Dispatchers.Main) {
                    detectionList = results
                    isDetecting = false
                }
            }
        },
        cameraBtnOnClick = {
            cameraLauncher.launch(null)
        },
        photoBtnOnClick = {
            photosLauncher.launch("image/*")
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DetectPage(
    inputImage: Bitmap?,
    detectionList: List<Detection>,
    isDetecting: Boolean,
    detectBtnOnClick: () -> Unit,
    cameraBtnOnClick: () -> Unit,
    photoBtnOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current

    Column(
        modifier = modifier
    ) {
        Text(
            text = "检测",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f))
        Row {
            Box(modifier = Modifier.weight(1f))
            if (isDetecting){
                LoadingIndicator()
            } else {
                if (inputImage != null) {
                    ScaledDetectionImage(
                        inputImage,
                        detectionList,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .heightIn(max = 380.dp)
                    )
                }
            }
            Box(modifier = Modifier.weight(1f))
        }
        Box(modifier = Modifier.weight(1f))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 100.dp)
        ) {
            Text("获取回收建议")
        }
        Box(modifier = Modifier.weight(0.5f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = cameraBtnOnClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("拍照")
            }
            Box(modifier = Modifier.width(16.dp))
            Button(
                onClick = photoBtnOnClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("相册选取")
            }
        }
        Button(
            onClick = {
                if (inputImage == null) {
                    Toast.makeText(localContext, "请先拍摄或选择图片!", Toast.LENGTH_SHORT).show()
                } else {
                    detectBtnOnClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("检测")
        }
    }
}


@Preview
@Composable
fun DetectPagePreview() {
    val exampleImage = ImageBitmap.imageResource(R.drawable.garbage02).asAndroidBitmap()
    val detectionList = listOf<Detection>()
    val isShowLoading = false
    DetectPage(
        exampleImage,
        detectionList,
        isShowLoading,
        detectBtnOnClick = {},
        cameraBtnOnClick = {},
        photoBtnOnClick = {},
        Modifier.fillMaxSize()
    )
}