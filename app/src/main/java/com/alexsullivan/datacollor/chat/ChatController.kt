package com.alexsullivan.datacollor.chat

import android.util.Log
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.chat.networking.OpenAIService
import com.alexsullivan.datacollor.chat.networking.models.AssistantTool
import com.alexsullivan.datacollor.chat.networking.models.CreateAssistant
import com.alexsullivan.datacollor.chat.networking.models.CreateAssistantResponse
import com.alexsullivan.datacollor.chat.networking.models.CreateMessage
import com.alexsullivan.datacollor.chat.networking.models.CreateRun
import com.alexsullivan.datacollor.chat.networking.models.DeleteFileResponse
import com.alexsullivan.datacollor.chat.networking.models.File
import com.alexsullivan.datacollor.chat.networking.models.FileUpload
import com.alexsullivan.datacollor.chat.networking.models.Message
import com.alexsullivan.datacollor.chat.networking.models.Run
import com.alexsullivan.datacollor.chat.networking.models.RunStatus
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import com.alexsullivan.datacollor.serialization.TrackableSerializer
import com.alexsullivan.datacollor.utils.flatMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ChatController @Inject constructor(
    private val openAIService: OpenAIService,
    private val getLifetimeData: GetLifetimeDataUseCase,
    private val prefs: QLPreferences
) {

    private var threadId: String? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    suspend fun initialize(): Result<*> {
        val deleteOldFileResult =
            prefs.openAiCsvFileId?.let { openAIService.deleteFile(it).toDeleteFileResponseResult() }
                ?: Result.success(Unit)
        val uploadFileResult = deleteOldFileResult.flatMap {
            uploadLatestCsv().onSuccess {
                prefs.openAiCsvFileId = it.id
            }
        }
        val createOrUpdateAssistantResult = uploadFileResult.flatMap {
            val existingAssistantId = prefs.openAiAssistantId
            if (existingAssistantId != null) {
               swapOutAssistantsFile(existingAssistantId, it.id)
            } else {
                createAssistant(it.id).onSuccess {
                    prefs.openAiAssistantId = it.id
                }
            }
        }

        return createOrUpdateAssistantResult.flatMap { openAIService.createThread().toResult() }
            .onSuccess {
                threadId = it.id
            }
    }

    suspend fun sendMessage(message: String): Result<*> {
        val threadId = threadId
        val assistantId = prefs.openAiAssistantId
        return if (threadId != null && assistantId != null) {
            Log.d("DEBUGGG", "Sending message $message")
            val createMessageResult =
                createMessage(threadId, message).onSuccess { createMessageResponse ->
                    val existingMessages = _messages.value
                    val newMessageList = listOf(createMessageResponse) + existingMessages
                    Log.d("DEBUGGG", "Sent message. Emitting new message")
                    _messages.emit(newMessageList)
                }
            createMessageResult.flatMap {
                Log.d("DEBUGGG", "Creating run")
                createRun(threadId, assistantId)
            }.onSuccess {
                GlobalScope.launch {
                    pollAndGetLatestMessages(it)
                }
            }
        } else {
            Result.failure<Run>(java.lang.IllegalStateException("No thread id or assistant id"))
        }
    }

    private suspend fun pollAndGetLatestMessages(run: Run) {
        pollRun(run).flatMap { openAIService.getMessages(run.thread_id).toResult() }
            .onSuccess { messageResponse ->
                _messages.emit(messageResponse.data)
            }
            .onFailure {
                Log.d("DEBUGGG", "Failed to get messages with error $it")
            }
    }

    private suspend fun pollRun(run: Run): Result<*> {
        var failCount = 0
        var status = RunStatus.from(run.status)
        while (status == RunStatus.QUEUED) {
            delay(2.seconds)
            val updatedRunResult =
                openAIService.getRun(run.thread_id, run.id).toResult()
            if (updatedRunResult.isFailure) {
                failCount++
                if (failCount > 5) {
                    return Result.failure<Unit>(IllegalStateException("Failed to get updated run status"))
                }
            }
            status = RunStatus.from(updatedRunResult.getOrThrow().status)
        }
        return if (status == RunStatus.COMPLETED) {
            Result.success(Unit)
        } else {
            Result.failure<Unit>(IllegalStateException("Run did not complete successfully"))
        }
    }

    private suspend fun createRun(
        threadId: String,
        assistantId: String
    ): Result<Run> {
        val createRun = CreateRun(assistantId)
        return openAIService.createRun(threadId, createRun).toResult()
    }

    private suspend fun createMessage(threadId: String, content: String): Result<Message> {
        val createMessage = CreateMessage(content = content)
        return openAIService.createMessage(threadId, createMessage).toResult()
    }

    private suspend fun uploadLatestCsv(): Result<File>  {
        val csvText = TrackableSerializer.serialize(getLifetimeData())
        val response = openAIService.uploadFile(FileUpload(csvText).toMultiPartBody())
        val responseBody = response.body()
        if (response.isSuccessful && responseBody != null) {
            return Result.success(responseBody)
        } else {
            return Result.failure(IllegalStateException("Failed to upload csv file"))
        }
    }

    private suspend fun createAssistant(csvFileId: String): Result<CreateAssistantResponse> {
        val createAssistant = CreateAssistant(
            instructions = instructions,
            name = name,
            tools = listOf(AssistantTool()),
            file_ids = listOf(csvFileId)
        )
        return openAIService.createAssistant(createAssistant).toResult()
    }

    private suspend fun swapOutAssistantsFile(
        assistantId: String,
        fileId: String
    ): Result<CreateAssistantResponse> {
        val createAssistant = CreateAssistant(
            instructions = instructions,
            name = name,
            tools = listOf(AssistantTool()),
            file_ids = listOf(fileId)
        )
        return openAIService.updateAssistant(assistantId, createAssistant).toResult()
    }

    private fun <T> Response<T>.toResult(): Result<T> {
        val body = body()
        return if (isSuccessful && body != null) {
            Result.success(body)
        } else {
            Result.failure(IllegalStateException(errorBody()?.string()))
        }
    }

    private fun Response<DeleteFileResponse>.toDeleteFileResponseResult(): Result<*> {
        // This can fail if we already deleted the file (like say we crashed after the delete file stage).
        // If it's a 404 we should tread that as a success.
        return if (raw().code == 404) {
            Result.success(Unit)
        } else {
            this.toResult()
        }
    }

    companion object {
        private const val instructions = "You are a personal data scientist for a user exploring their data in the attached file. You only answer questions that correspond to a user exploring the data in the attached file. You are creative and think out of the box, offering non obvious correlations and statistics for the user."
        private const val name = "Your Personal Data Scientist"
    }
}
