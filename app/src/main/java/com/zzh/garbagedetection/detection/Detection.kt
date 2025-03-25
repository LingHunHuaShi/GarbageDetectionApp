package com.zzh.garbagedetection.detection

import android.graphics.RectF

data class Detection(
    val boundingBox: RectF,
    val label: String,
    val score: Float
)
