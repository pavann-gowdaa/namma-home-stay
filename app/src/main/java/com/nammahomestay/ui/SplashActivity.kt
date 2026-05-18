package com.nammahomestay.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.nammahomestay.MainActivity
import com.nammahomestay.R
import com.nammahomestay.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var shimmerLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        shimmerLayout = findViewById(R.id.shimmerLayout)
        shimmerLayout.startShimmer()

        Handler(Looper.getMainLooper()).postDelayed({
            shimmerLayout.stopShimmer()
            val intent = Intent(this, MainActivity::class.java)
            val sessionManager = SessionManager(this)
            if (sessionManager.isLoggedIn) {
                intent.putExtra("auto_role", sessionManager.getUserRole())
            }
            startActivity(intent)
            finish()
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        if (::shimmerLayout.isInitialized) shimmerLayout.stopShimmer()
    }
}