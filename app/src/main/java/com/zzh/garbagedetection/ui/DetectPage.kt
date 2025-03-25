package com.zzh.garbagedetection.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.graphics.scale
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
    val context = LocalContext.current
    var displayImage by remember { mutableStateOf<Bitmap?>(null) }
    var detectionList by remember { mutableStateOf(listOf<Detection>()) }

    val scope = rememberCoroutineScope()

    // Launch once on composition
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val decoded = BitmapFactory.decodeResource(context.resources, R.drawable.garbage02)
            val scaled = decoded.scale(1200, 900)
            withContext(Dispatchers.Main) { displayImage = scaled }
        }
    }

    // Until loaded, show a placeholder
    val image = displayImage ?: return LoadingIndicator()

    DetectPage(
        image,
        detectionList,
        detectBtnOnClick = {
            scope.launch(Dispatchers.Default) {
                val results = when(modelName) {
                    YOLO.label -> detectImageYolo(image, context)
                    GOOGLE.label -> detectImageGoogle(image, context)
                    else -> emptyList()
                }
                withContext(Dispatchers.Main) {
                    detectionList = results
                }
            }
        },
        cameraBtnOnClick = {

        },
        photoBtnOnClick = {

        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DetectPage(
    inputImage: Bitmap,
    detectionList: List<Detection>,
    detectBtnOnClick: () -> Unit,
    cameraBtnOnClick: () -> Unit,
    photoBtnOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "检测",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Box(modifier = Modifier.height(120.dp))
        ScaledDetectionImage(
            inputImage,
            detectionList,
            0.3f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(modifier = Modifier.weight(1f))
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
            onClick = detectBtnOnClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 48.dp)
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
    DetectPage(
        exampleImage,
        detectionList,
        detectBtnOnClick = {},
        cameraBtnOnClick = {},
        photoBtnOnClick = {},
        Modifier.fillMaxSize()
    )
}