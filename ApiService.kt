package com.example.videystar

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.* // Ini akan mencakup GET, POST, Query, Field, FormUrlEncoded secara otomatis

interface ApiService {
    @GET("api/api_get_videos.php")
    fun getVideos(@Query("page") page: Int): Call<VideoResponse>

    // --- TAMBAHKAN KODE BARU DI SINI ---
    @GET("api/api_get_random.php")
    fun getRandomVideos(
        @Query("category_id") categoryId: Int,
        @Query("current_video_id") currentVideoId: Int
    ): Call<VideoResponse>

    @POST("api/api_update_stats.php")
    @FormUrlEncoded
    fun updateStats(
        @Field("video_id") videoId: Int,
        @Field("action") action: String 
    ): Call<Void>
    // -----------------------------------

    companion object {
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
