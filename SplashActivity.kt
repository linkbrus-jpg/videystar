package com.example.videystar // Sesuaikan dengan nama package Anda

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Pastikan Anda punya layout untuk splash ini (misal logo aplikasi)

        auth = FirebaseAuth.getInstance()

        // Loading 2 detik (2000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, 2000)
    }

    private fun checkUserLoginStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Jika belum login, arahkan ke Halaman Login Google
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Jika sudah login, tampilkan peringatan umur 18+
            showAgeWarningDialog()
        }
    }

    private fun showAgeWarningDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_age_warning, null)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Tidak bisa ditutup dengan klik luar kotak
            .create()

        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)

        btnYes.setOnClickListener {
            dialog.dismiss()
            // Lanjut ke Halaman Utama (MainActivity)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
            // Keluar dari aplikasi jika belum 18+
            finishAffinity()
        }

        dialog.show()
    }
}
