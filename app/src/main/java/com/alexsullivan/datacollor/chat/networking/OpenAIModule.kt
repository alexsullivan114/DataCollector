package com.alexsullivan.datacollor.chat.networking

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    @Provides
    fun provideOpenAIService(): OpenAIService {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor {  chain ->
                val updatedHeaders = chain.request().headers.newBuilder()
                        // TODO: Move API keys out of here into gradle file
                    .add("Authorization", "Bearer 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")
                    .add("OpenAI-Beta", "assistants=v1")
                    .add("Content-Type", "application/json").build()
                chain.proceed(chain.request().newBuilder().headers(updatedHeaders).build())
            }
            .build()
        val retrofit =
            Retrofit.Builder().baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client).build()
        return retrofit.create(OpenAIService::class.java)
    }
}
