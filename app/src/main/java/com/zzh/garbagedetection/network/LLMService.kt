package com.zzh.garbagedetection.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import kotlin.getValue

interface LLMService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun getLlmResponse(
        @Header("Authorization") authHead: String,

        @Body postBody: PostBody
    ): Response<PostResponse>
}

object RetrofitClient{
    private const val BASE_URL = "https://api.aigc369.com/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    val llmService: LLMService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LLMService::class.java)
    }
}

fun createPostBodyUsingDefaultPrompt(imageBase64: String): PostBody {
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
                        type = "image_url",
                        content = null,
                        imageUrl = ImageUrl(
                            url = "data:image/jpeg;base64,$imageBase64"
                        )
                    )
                )
            )
        )
    )
    return postBody
}


data class PostBody(
    @SerializedName("model")
    val modelName: String,

    @SerializedName("messages")
    val messages: List<ModelMessage>
)

data class PostResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("object")
    val obj: String,

    @SerializedName("created")
    val created: Long,

    @SerializedName("choices")
    val choices: List<ResponseMessage>
)

data class ModelMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: List<MessageContent>,
)

data class MessageContent(
    @SerializedName("type")
    val type: String,

    @SerializedName("content")
    val content: String?,

    @SerializedName("image_url")
    val imageUrl: ImageUrl?
)

data class ImageUrl(
    @SerializedName("url")
    val url: String,
)

data class ResponseMessage(
    @SerializedName("index")
    val index: String,

    @SerializedName("message")
    val message: ResponseMessageContent
)

data class ResponseMessageContent(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String,
)