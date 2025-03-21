package com.zzh.garbagedetection

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzh.garbagedetection.detection.mapOutputsToDetections
import com.zzh.garbagedetection.ui.components.ScaledDetectionImage
import com.zzh.garbagedetection.ui.theme.GarbageDetectionTheme
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GarbageDetectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val compatList = CompatibilityList()
    val context = LocalContext.current

//    val modelBuffer = FileUtil.loadMappedFile(context, "ssd_model.tflite")
    val modelBuffer = FileUtil.loadMappedFile(context, "lite-model_efficientdet_lite0_detection_metadata_1.tflite")

    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.garbage02)
    var tensorImage = TensorImage(DataType.UINT8)
    tensorImage.load(bitmap)

    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
        .build()
    tensorImage = imageProcessor.process(tensorImage)
    // 2️⃣ Build Interpreter with FlexDelegate enabled
    val tfliteOptions = Interpreter.Options().apply {
        addDelegate(FlexDelegate())
        setNumThreads(4)
    }
    val interpreter = Interpreter(modelBuffer, tfliteOptions)
    val inputBuffer = tensorImage.buffer
    // 4️⃣ Dynamically query output shapes
// 读取 boxes 输出 tensor shape 来获取 N
    val N = interpreter.getOutputTensor(1).shape()[1]

//    val outputBoxes   = Array(1) { Array(N) { FloatArray(4) } }
//    val outputClasses = Array(1) { FloatArray(N) }
//    val outputScores  = Array(1) { FloatArray(N) }
//    val outputCount   = IntArray(1)
//
//    val outputMap = mapOf(
//        0 to outputScores,
//        1 to outputBoxes,
//        2 to outputCount,
//        3 to outputClasses
//    )
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

    // 6️⃣ Run inference
    Log.d("ObjectDetector", "Inference start")
    interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)
    Log.d("ObjectDetector", "Inference done")

    // 7️⃣ Parse results
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
    val detectionList = mapOutputsToDetections(
        outputBoxes = outputBoxes,
        outputClasses = outputClasses,
        outputScores = outputScores,
        outputCount = outputCount,
        labels = listOf("可回收垃圾","有害垃圾","湿垃圾","干垃圾")
    )

    ScaledDetectionImage(
        bitmap = bitmap,
        detections = detectionList,
        threshold = 0.3f,
        modifier = Modifier.padding(horizontal = 50.dp, vertical = 100.dp)
    )

}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GarbageDetectionTheme {
        Greeting("Android")
    }
}