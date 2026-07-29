package com.example.rooyify.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitClient {
    // Hosted production backend URL (Replace this with your deployed Render / PythonAnywhere URL)
    private const val BASE_URL = "https://rooyify-backend.onrender.com/"
    
    // Local development fallback (Wi-Fi network IP / Emulator loopback):
    // private const val BASE_URL = "http://10.0.2.2:5000/"
    // private const val BASE_URL = "http://192.168.31.82:5000/"

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
