package com.example.rooyify.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitClient {
    // Local Flask development backend (Wi-Fi network IP for physical device / emulator access)
    private const val BASE_URL = "http://192.168.31.82:5000/oct/spic_726/hairjourney/"
    // Emulator loopback IP:
    // private const val BASE_URL = "http://10.0.2.2:5000/oct/spic_726/hairjourney/"
    // Remote original URL:
    // private const val BASE_URL = "http://14.139.187.229:8081/oct/spic_726/hairjourney/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
        retrofit.create(ApiService::class.java)
    }
}
