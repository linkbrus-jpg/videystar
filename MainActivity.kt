package com.example.videystar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videystar.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val apiService = ApiService.create()
    
    private var currentPage = 1
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Setup Toolbar dan Menu Garis 3
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 2. Setup Grid Layout Video (Menampilkan 2 kolom grid)
        recyclerView = findViewById(R.id.rv_videos)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        videoAdapter = VideoAdapter(arrayListOf()) { video ->
            // Aksi ketika video diklik (Nanti diarahkan ke Watch Activity)
            Toast.makeText(this, "Membuka: ${video.title}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = videoAdapter

        // 3. Ambil data video Halaman 1 dari API
        loadVideos(currentPage)

        // 4. Deteksi scroll untuk Pagination (Pindah ke halaman berikutnya setelah 12 baris)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount 
                        && firstVisibleItemPosition >= 0) {
                        
                        // Jika sudah mencapai batas bawah video ke-12, load halaman berikutnya
                        currentPage++
                        loadVideos(currentPage)
                    }
                }
            }
        })
    }

    private fun loadVideos(page: Int) {
        isLoading = true
        apiService.getVideos(page).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    response.body()?.data?.let {
                        videoAdapter.addData(it)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Tidak ada video lagi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(this@MainActivity, "Gagal memuat data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
