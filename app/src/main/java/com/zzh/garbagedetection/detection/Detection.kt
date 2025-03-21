package com.zzh.garbagedetection.detection

import android.graphics.RectF

data class Detection(
    val boundingBox: RectF,
    val label: String,
    val score: Float
)

fun mapOutputsToDetections(
    outputBoxes: Array<Array<FloatArray>>,
    outputClasses: Array<FloatArray>,
    outputScores: Array<FloatArray>,
    outputCount: FloatArray,
    labels: List<String>
): List<Detection> {
    val detections = mutableListOf<Detection>()
    val count = outputCount[0].toInt().coerceAtMost(outputBoxes[0].size)

    for (i in 0 until count) {
        val box = outputBoxes[0][i]
        val ymin = box[0]
        val xmin = box[1]
        val ymax = box[2]
        val xmax = box[3]

        // 创建归一化的 RectF(left, top, right, bottom)
        val rect = RectF(xmin, ymin, xmax, ymax)

        // 获取类别索引、标签和分数
        val classIndex = outputClasses[0][i]
        val label = labels.getOrNull(classIndex.toInt()) ?: "Unknown"
        val score = outputScores[0][i]

        detections += Detection(rect, label, score)
    }
    return detections
}
