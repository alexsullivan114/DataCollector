package com.alexsullivan.datacollor.chat.networking

import com.alexsullivan.datacollor.BuildConfig
import com.alexsullivan.datacollor.utils.JSON
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    @Provides
    fun provideOpenAIService(): OpenAIService {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor {  chain ->
                val updatedHeaders = chain.request().headers.newBuilder()
                    .add("Authorization", "Bearer ${BuildConfig.OPENAI_KEY}")
                    .add("OpenAI-Beta", "assistants=v1")
                    .add("Content-Type", "application/json").build()
                chain.proceed(chain.request().newBuilder().headers(updatedHeaders).build())
            }
            .build()
        val retrofit =
            Retrofit.Builder().baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(
                    JSON.asConverterFactory(
                        "application/json; charset=UTF8".toMediaType()))
                .client(client).build()
        return retrofit.create(OpenAIService::class.java)
    }
}
