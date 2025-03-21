package com.zzh.garbagedetection.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.zzh.garbagedetection.detection.Detection
import kotlin.collections.forEach
import androidx.core.graphics.scale

@Composable
fun ScaledDetectionImage(
    bitmap: Bitmap,
    detections: List<Detection>,
    threshold: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    // 获取屏幕宽度（px）
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val density = LocalDensity.current
    val targetWidthPx = with(density) { screenWidth.toPx() }.toInt()
    val aspectRatio = bitmap.height.toFloat() / bitmap.width

    // 计算目标尺寸
    val scaledBitmap = remember(bitmap) {
        bitmap.scale(targetWidthPx, (targetWidthPx * aspectRatio).toInt())
    }

    DetectionOverlay(
        bitmap = scaledBitmap,
        detections = detections,
        threshold = threshold,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / aspectRatio)
    )
}

@Composable
fun DetectionOverlay(
    bitmap: Bitmap,
    detections: List<Detection>,
    threshold: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    // 保持图片比例
    val aspect = bitmap.width.toFloat() / bitmap.height
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.matchParentSize()
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            detections.forEach { det ->
                if (det.score >= threshold) {
                    val box = det.boundingBox
                    val left = box.left * size.width
                    val top = box.top * size.height
                    val width = (box.right - box.left) * size.width
                    val height = (box.bottom - box.top) * size.height

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "${det.label} ${(det.score * 100).toInt()}%",
                            left,
                            top - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.RED
                                textSize = 32f
                                style = android.graphics.Paint.Style.FILL
                            }
                        )
                    }
                }
            }
        }
    }
}