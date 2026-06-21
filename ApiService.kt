package com.example.videystar

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson:GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/api_get_videos.php")
    fun getVideos(@Query("page") page: Int): Call<VideoResponse>

    companion object {
        // Ganti dengan alamat domain Anda
        private const val BASE_URL = "https://videystar.pro/" 

        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
