package com.zzh.garbagedetection.network

import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface LLMService {

    @Headers("Content-Type: application/json")
    @POST
    fun getLlmResponse(
        @Header("Authorization") authHead: String,
        @Body postBody: PostBody
    )
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
    val message: List<ResponseMessageContent>
)

data class ResponseMessageContent(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String,
)