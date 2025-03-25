package com.zzh.garbagedetection.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp


object GoogleDetectionConstants {
    const val MODEL_PATH = "lite-model_efficientdet_lite0_detection_metadata_1.tflite"
    val INPUT_DTYPE = DataType.UINT8
    const val INPUT_SIZE = 320
}

fun detectImageGoogle(inputImage: Bitmap, context: Context): List<Detection> {
    val modelBuffer = FileUtil.loadMappedFile(context, GoogleDetectionConstants.MODEL_PATH)
    var tensorImage = TensorImage(GoogleDetectionConstants.INPUT_DTYPE)
    tensorImage.load(inputImage)
    val inputSize = GoogleDetectionConstants.INPUT_SIZE

    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    tensorImage = imageProcessor.process(tensorImage)

    val tfliteOptions = Interpreter.Options().apply {
        addDelegate(FlexDelegate())
        setNumThreads(4)
    }
    val interpreter = Interpreter(modelBuffer, tfliteOptions)
    val inputBuffer = tensorImage.buffer

    // Create output map
    val N = interpreter.getOutputTensor(1).shape()[1]
    val outputBoxes   = Array(1) { Array(N) { FloatArray(4) } }
    val outputClasses = Array(1) { FloatArray(N) }
    val outputScores  = Array(1) { FloatArray(N) }
    val outputCount   = FloatArray(1)

    val outputMap = mapOf(
        0 to outputBoxes,
        1 to outputClasses,
        2 to outputScores,
        3 to outputCount
    )

    for (i in 0 until interpreter.outputTensorCount) {
        val tensor = interpreter.getOutputTensor(i)
        Log.i("TFLite", "Output[$i] name=${tensor.name()} shape=${tensor.shape().contentToString()} dtype=${tensor.dataType()}")
    }

    //Run inference
    Log.d("ObjectDetector", "Inference start")
    interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)
    Log.d("ObjectDetector", "Inference done")

    //Parse results
    val detections = outputCount[0].toInt()
    for (i in 0 until detections) {
        val box = outputBoxes[0][i]
        val label = outputClasses[0][i].toInt()
        val score = outputScores[0][i]
        Log.d(
            "ObjectDetector",
            "Detected class=$label score=${"%.2f".format(score)} box=${box.contentToString()}"
        )
    }
    Log.d("ObjectDetector", "Inference complete: $detections results")
    val detectionList = mapOutputsToGoogleDetections(
        outputBoxes = outputBoxes,
        outputClasses = outputClasses,
        outputScores = outputScores,
        outputCount = outputCount,
        labels = listOf("可回收垃圾","有害垃圾","湿垃圾","干垃圾")
    )
    interpreter.close()
    return detectionList
}

fun mapOutputsToGoogleDetections(
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