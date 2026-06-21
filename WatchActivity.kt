package com.example.videystar

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WatchActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var rvRandom: RecyclerView
    private lateinit var randomAdapter: VideoAdapter
    private val apiService = ApiService.create()

    private var videoId = 0
    private var categoryId = 0
    private var isDownloadFullOn = 0
    private var downloadFullUrl: String? = null
    private var videoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch)

        // 1. Tangkap Data dari Intent
        videoId = intent.getIntExtra("VIDEO_ID", 0)
        categoryId = intent.getIntExtra("CATEGORY_ID", 0)
        val title = intent.getStringExtra("TITLE") ?: ""
        videoUrl = intent.getStringExtra("VIDEO_URL")
        isDownloadFullOn = intent.getIntExtra("IS_DOWNLOAD_FULL", 0)
        downloadFullUrl = intent.getStringExtra("DOWNLOAD_FULL_URL")
        val views = intent.getIntExtra("VIEWS", 0)

        // 2. Inisialisasi Komponen UI
        playerView = findViewById(R.id.player_view)
        findViewById<TextView>(R.id.tv_watch_title).text = title
        findViewById<TextView>(R.id.tv_watch_views).text = "${views + 1} x ditonton"
        findViewById<TextView>(R.id.tv_watch_category).text = "ID Kategori: $categoryId"

        val btnDownloadNormal = findViewById<Button>(R.id.btn_download_normal)
        val btnDownloadFull = findViewById<Button>(R.id.btn_download_full)

        // 3. Logika Tombol Download Dinamis Sesuai Permintaan Anda
        if (isDownloadFullOn == 1) {
            // Jika ON: Tombol download biasa HILANG, tombol download full MUNCUL
            btnDownloadNormal.visibility = View.GONE
            btnDownloadFull.visibility = View.VISIBLE
        } else {
            // Jika OFF: Tombol download biasa MUNCUL, tombol download full HILANG
            btnDownloadNormal.visibility = View.VISIBLE
            btnDownloadFull.visibility = View.GONE
        }

        // Aksi Tombol Download Biasa (Langsung Download di Aplikasi)
        btnDownloadNormal.setOnClickListener {
            videoUrl?.let { url ->
                downloadVideoDirectly(url, title)
                updateStatsToServer("download")
            } ?: Toast.makeText(this, "URL video tidak tersedia", Toast.LENGTH_SHORT).show()
        }

        // Aksi Tombol Download Full (Dialihkan ke Browser/Chrome)
        btnDownloadFull.setOnClickListener {
            if (!downloadFullUrl.isNullOrEmpty()) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadFullUrl))
                startActivity(browserIntent)
                updateStatsToServer("full_download")
            } else {
                Toast.makeText(this, "Link download full kosong", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Share & Like Ringan
        findViewById<Button>(R.id.btn_share).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Tonton video seru '$title' di aplikasi Videystar.pro!")
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan via"))
        }

        // 4. Jalankan Video & Kirim Statistik View (+1) ke Server
        initializePlayer()
        updateStatsToServer("view")

        // 5. Muat Video Rekomendasi (Random se-kategori)
        setupRandomVideos()
    }

    private fun initializePlayer() {
        if (videoUrl.isNullOrEmpty()) return
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true // Otomatis putar video saat halaman terbuka
        }
    }

    private fun downloadVideoDirectly(url: String, title: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("Mengunduh video...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$title.mp4")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "Unduhan dimulai. Cek panel notifikasi Anda.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengunduh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRandomVideos() {
        rvRandom = findViewById(R.id.rv_random_videos)
        rvRandom.layoutManager = GridLayoutManager(this, 2)
        randomAdapter = VideoAdapter(arrayListOf()) { selectedVideo ->
            // Ketika video terkait diklik, muat ulang halaman tonton dengan data baru
            val intent = Intent(this, WatchActivity::class.java).apply {
                putExtra("VIDEO_ID", selectedVideo.id)
                putExtra("CATEGORY_ID", selectedVideo.category_id)
                putExtra("TITLE", selectedVideo.title)
                putExtra("VIDEO_URL", selectedVideo.video_url)
                putExtra("IS_DOWNLOAD_FULL", selectedVideo.is_download_full_on)
                putExtra("DOWNLOAD_FULL_URL", selectedVideo.download_full_url)
                putExtra("VIEWS", selectedVideo.views_count)
            }
            startActivity(intent)
            finish() // Tutup halaman lama agar memori efisien
        }
        rvRandom.adapter = randomAdapter

        // Panggil API untuk mendapatkan video random dari kategori yang sama
        apiService.getRandomVideos(categoryId, videoId).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    response.body()?.data?.let { randomAdapter.addData(it) }
                }
            }
            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {}
        })
    }

    private fun updateStatsToServer(action: String) {
        apiService.updateStats(videoId, action).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    override fun onStop() {
        super.onStop()
        // Hentikan video saat user keluar aplikasi atau meminimize agar tidak memakan baterai
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
