package com.zzh.garbagedetection.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import kotlin.getValue

interface LLMService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun getLlmResponse(
        @Header("Authorization") authHead: String,

        @Body postBody: PostBody
    ): Call<PostResponse>
}

object RetrofitClient{
    private const val BASE_URL = "https://api.aigc369.com/v1/"
    val llmService: LLMService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LLMService::class.java)
    }
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