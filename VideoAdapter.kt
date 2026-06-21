package com.example.videystar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(
    private val videoList: ArrayList<VideoItem>,
    private val onItemClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumbnail: ImageView = view.findViewById(R.id.img_thumbnail)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvViews: TextView = view.findViewById(R.id.tv_views)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.tvTitle.text = video.title
        holder.tvViews.text = "${video.views_count} x ditonton"

        // Memuat gambar thumbnail menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(video.thumbnail_url)
            .placeholder(android.R.color.darker_gray)
            .into(holder.imgThumbnail)

        holder.itemView.setOnClickListener { onItemClick(video) }
    }

    override fun getItemCount(): Int = videoList.size

    fun addData(newData: List<VideoItem>) {
        val startPos = videoList.size
        videoList.addAll(newData)
        notifyItemRangeInserted(startPos, newData.size)
    }
}
