package com.zzh.garbagedetection.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.zzh.garbagedetection.network.ImageUrl
import com.zzh.garbagedetection.network.ModelMessage
import com.zzh.garbagedetection.network.PostBody
import com.zzh.garbagedetection.network.PostResponse
import com.zzh.garbagedetection.network.RetrofitClient
import com.zzh.garbagedetection.network.assistantPrompt
import com.zzh.garbagedetection.network.createPostBodyUsingDefaultPrompt
import com.zzh.garbagedetection.ui.LoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ExpandableButton(
    imageBase64: String,
    modifier: Modifier = Modifier
) {
    val TAG = "Expandable Button"

    var isExpanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

//    val TOKEN = "Bearer sk-JHYRmWW4BROETYKBMCXp8sNz4aTVZUTFCfX8bcGd0nGG1Bf2"
    val TOKEN = "Bearer sk-uadRZjrbvhe6L7nANkrU8gscMBtTQ76O0gH6Q5DVuoUh9wqD"
    val localContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val gson = Gson()

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            if (!isLoading) {
                Text(text = text?:"")
            } else {
                Row {
                    Box(modifier = Modifier.weight(1f))
                    LoadingIndicator()
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                if (imageBase64.isEmpty()) {
                    Toast.makeText(localContext, "请先完成垃圾检测", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isExpanded) {
                        isLoading = true
                        isExpanded = true

                        var responseCode: Int = 0

                        val postBody = createPostBodyUsingDefaultPrompt(imageBase64)
                        Log.d(TAG, "Request body: ${gson.toJson(postBody)}")

                        coroutineScope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    val res = RetrofitClient.llmService.getLlmResponse(
                                        authHead = TOKEN,
                                        postBody = postBody
                                    )
                                    isLoading = false
                                    res
                                }
                                withContext(Dispatchers.Main) {
                                    responseCode = response.code()
                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        text = body?.choices[0]?.message?.content
                                        Log.d(TAG, "Request success: code:${response.code()}")
                                    } else {
                                        val errorBody = response.errorBody()
                                        val errorMsg = errorBody?.string()
                                        text = "Error: Code $responseCode, $errorMsg"
                                        Log.e(TAG, "Request failed: code:$responseCode, message:$errorMsg")
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Log.e(TAG, "Request Exception: ${e.message}")
                                    isLoading = false
                                    text = "Error: Code $responseCode, ${e.message}"
                                }
                            }
                        }
                    } else {
                        isExpanded = false
                        text = null
                    }
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = if (!isExpanded) "获取回收建议" else "关闭")
        }
    }
}

@Preview
@Composable
fun ExpandableButtonPreview() {
    ExpandableButton(
        imageBase64 = ""
    )
}