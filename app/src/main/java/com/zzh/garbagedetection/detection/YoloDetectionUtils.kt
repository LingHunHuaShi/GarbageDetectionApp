package com.zzh.garbagedetection.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

object YoloDetectionUtils {
    const val MODEL_PATH = "best_float32.tflite"
    val INPUT_DTYPE = DataType.FLOAT32
    const val INPUT_SIZE = 640
}

fun detectImageYolo(inputImage: Bitmap, threshold: Float = 0.5f, context: Context): List<Detection> {
    val modelBuffer = FileUtil.loadMappedFile(context, YoloDetectionUtils.MODEL_PATH)
    var tensorImage = TensorImage(YoloDetectionUtils.INPUT_DTYPE)
    tensorImage.load(inputImage)
    val inputSize = YoloDetectionUtils.INPUT_SIZE

    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    tensorImage = imageProcessor.process(tensorImage)

    val tfliteOptions = Interpreter.Options().apply {
        addDelegate(FlexDelegate())
        setNumThreads(4)
    }
    val interpreter = Interpreter(modelBuffer, tfliteOptions)
    val inputBuffer = tensorImage.buffer


    for (i in 0 until interpreter.outputTensorCount) {
        val tensor = interpreter.getOutputTensor(i)
        Log.i("TFLite", "Output[$i] name=${tensor.name()} shape=${tensor.shape().contentToString()} dtype=${tensor.dataType()}")
    }

    // Create output buffer
    val outputBuffer = Array(1) { Array(300) { FloatArray(6) } }

    //Run inference
    Log.d("ObjectDetector", "Inference start")
    interpreter.run(inputBuffer, outputBuffer)
    Log.d("ObjectDetector", "Inference done")

    val labels = listOf("可回收垃圾","有害垃圾","湿垃圾","干垃圾")
    val detectionList = mutableListOf<Detection>()

    for (i in 0 until 300) {
        val score = outputBuffer[0][i][4]
        if (score >= threshold) {
            val xMin = outputBuffer[0][i][0]
            val yMin = outputBuffer[0][i][1]
            val xMax = outputBuffer[0][i][2]
            val yMax = outputBuffer[0][i][3]
            val classId = outputBuffer[0][i][5].toInt()

            val boundingBox = RectF(xMin, yMin, xMax, yMax)
            detectionList += Detection(boundingBox = boundingBox, label = labels[classId], score = score)
        }
    }
    Log.d("YOLO output", "Detection number: ${detectionList.size}")
    for (item in detectionList) {
        Log.d("YOLO output", "Detection: $item")
    }
    return detectionList
}