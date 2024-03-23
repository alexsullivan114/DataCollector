package com.alexsullivan.datacollor.chat.networking

import com.alexsullivan.datacollor.chat.networking.models.CreateAssistant
import com.alexsullivan.datacollor.chat.networking.models.CreateAssistantResponse
import com.alexsullivan.datacollor.chat.networking.models.CreateMessage
import com.alexsullivan.datacollor.chat.networking.models.CreateRun
import com.alexsullivan.datacollor.chat.networking.models.DeleteFileResponse
import com.alexsullivan.datacollor.chat.networking.models.File
import com.alexsullivan.datacollor.chat.networking.models.GetFilesResponse
import com.alexsullivan.datacollor.chat.networking.models.Message
import com.alexsullivan.datacollor.chat.networking.models.MessageResponse
import com.alexsullivan.datacollor.chat.networking.models.Run
import com.alexsullivan.datacollor.chat.networking.models.Thread
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface OpenAIService {
    @Multipart
    @POST("files")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody = "assistants".toRequestBody("text/plain".toMediaType())
    ): Response<File>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<DeleteFileResponse>

    @POST("assistants")
    suspend fun createAssistant(@Body createAssistant: CreateAssistant): Response<CreateAssistantResponse>

    @POST("assistants/{assistantId}")
    suspend fun updateAssistant(@Path("assistantId") assistantId: String, @Body createAssistant: CreateAssistant): Response<CreateAssistantResponse>

    @POST("threads")
    suspend fun createThread(): Response<Thread>

    @POST("threads/{threadId}/messages")
    suspend fun createMessage(@Path("threadId") threadId: String, @Body message: CreateMessage): Response<Message>

    @POST("threads/{threadId}/runs")
    suspend fun createRun(@Path("threadId") threadId: String, @Body run: CreateRun): Response<Run>

    @GET("threads/{threadId}/runs/{runId}")
    suspend fun getRun(@Path("threadId") threadId: String, @Path("runId") runId: String): Response<Run>

    @GET("threads/{threadId}/messages")
    suspend fun getMessages(@Path("threadId") threadId: String): Response<MessageResponse>

    @GET("files")
    suspend fun getAllFiles(): Response<GetFilesResponse>

    @GET("files/{fileId}/content")
    suspend fun getFileContent(@Path("fileId") fileId: String): Response<ResponseBody>
}
