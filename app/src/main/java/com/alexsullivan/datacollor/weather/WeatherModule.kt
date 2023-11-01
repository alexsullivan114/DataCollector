package com.alexsullivan.datacollor.weather

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {
    @Provides
    fun providesWeatherService(): WeatherService {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(Level.BASIC)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val retrofit =
            Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/3.0/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client).build()
        return retrofit.create(WeatherService::class.java)
    }
}
