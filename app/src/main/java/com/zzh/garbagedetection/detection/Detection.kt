package com.zzh.garbagedetection.detection

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Detection(
    val boundingBox: RectF,
    val label: String,
    val score: Float
)
