package com.alexsullivan.datacollor.chat.networking.models

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


data class FileUpload(val fileContent: String) {
    fun toMultiPartBody(): MultipartBody.Part {
        val requestBody: RequestBody = fileContent.toRequestBody("text/csv".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "quantified-life.csv", requestBody)
    }
}
