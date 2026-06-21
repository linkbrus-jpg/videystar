package com.example.videystar

data class VideoResponse(
    val status: String,
    val page: Int,
    val data: List<VideoItem>
)

data class VideoItem(
    val id: Int,
    val category_id: Int,
    val title: String,
    val thumbnail_url: String,
    val video_url: String?,
    val is_download_full_on: Int,
    val download_full_url: String?,
    val views_count: Int
)
