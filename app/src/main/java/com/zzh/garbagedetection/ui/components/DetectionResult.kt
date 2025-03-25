package com.zzh.garbagedetection.ui.components

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
    threshold: Float,
    modifier: Modifier = Modifier
) {
    // 获取屏幕宽度（px）
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val density = LocalDensity.current

    val aspect = bitmap.width.toFloat() / bitmap.height
    val targetWidthPx = with(density) { screenWidth.toPx() }.toInt()
    val targetHeightPx = (targetWidthPx / aspect).toInt()

    val resizedBitmap = bitmap.scale(targetWidthPx, targetHeightPx)

    // 保持图片比例

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
    ) {
        Image(
            bitmap = resizedBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.matchParentSize()
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 4.dp.toPx()
//            val paint = Paint().apply {
//                color = Color.Red
//                style = PaintingStyle.Stroke
//                strokeWidth = stroke
//            }
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 32f
            }
            detections.forEach { det ->
                if (det.score >= threshold) {
                    val l = det.boundingBox.left * size.width
                    val t = det.boundingBox.top * size.height
                    val r = det.boundingBox.right * size.width
                    val b = det.boundingBox.bottom * size.height

                    drawRect(
                        topLeft = Offset(l, t),
                        size = Size(r - l, b - t),
                        style = Stroke(width = stroke),
                        color = Color.Red
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "${det.label} ${(det.score * 100).toInt()}%",
                        l,
                        t - stroke,
                        textPaint
                    )
                }
            }
            Log.d("Detection Res Drawing", "ScaledDetectionImage: Detection drawing done")
        }
    }
}