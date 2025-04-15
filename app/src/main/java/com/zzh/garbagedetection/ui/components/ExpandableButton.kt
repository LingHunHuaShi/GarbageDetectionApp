package com.zzh.garbagedetection.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.zzh.garbagedetection.network.ImageUrl
import com.zzh.garbagedetection.network.MessageContent
import com.zzh.garbagedetection.network.ModelMessage
import com.zzh.garbagedetection.network.PostBody
import com.zzh.garbagedetection.network.PostResponse
import com.zzh.garbagedetection.network.RetrofitClient
import com.zzh.garbagedetection.network.assistantPrompt
import com.zzh.garbagedetection.ui.LoadingIndicator
import kotlinx.coroutines.launch
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

    val TOKEN = "Bearer sk-JHYRmWW4BROETYKBMCXp8sNz4aTVZUTFCfX8bcGd0nGG1Bf2"

    val localContext = LocalContext.current

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
            text?.let {
                if (!isLoading){
                    Text(text = it)
                } else {
                    LoadingIndicator()
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

                        val postBody = PostBody(
                            modelName = "gpt-4o",
                            messages = listOf(
                                ModelMessage(
                                    role = "user",
                                    content = listOf(
                                        MessageContent(
                                            type = "text",
                                            content = assistantPrompt,
                                            imageUrl = null
                                        ),
                                        MessageContent(
                                            type = "image",
                                            content = null,
                                            imageUrl = ImageUrl(
                                                url = "data:image/jpeg;base64,$imageBase64"
                                            )
                                        )
                                    )
                                )
                            )
                        )

                        try {
                            val call = RetrofitClient.llmService.getLlmResponse(
                                authHead = TOKEN,
                                postBody = postBody
                            )
                            isExpanded = true
                            call.enqueue(object : Callback<PostResponse> {
                                override fun onResponse(
                                    p0: Call<PostResponse?>,
                                    p1: Response<PostResponse?>
                                ) {
                                    val body = p1.body()
                                    text = body?.choices[0]?.message?.content
                                    isLoading = false
                                }

                                override fun onFailure(
                                    p0: Call<PostResponse?>,
                                    p1: Throwable
                                ) {
                                    text = p1.message
                                    isLoading = false
                                }
                            })

                        } catch (e: Exception) {
                            Log.d(TAG, "ExpandableButton: ${e.message}")
                            text = "Error: ${e.message}"
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